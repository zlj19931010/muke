/*
 * asdasasd
 */

//存放主要交互逻辑的js代码
var seckill = {
    //封装秒杀相关的url
    URL: {
        time_now: function () {
            return "/seckill/time/now";
        },
        exposer: function (seckillId) {
            return "/seckill/" + seckillId + "/exposer";
        },
        execution: function (seckillId, md5) {
            return "/seckill/" + seckillId + "/" + md5 + "/execution";
        }
    },
    validatePhone: function (phone) {
        if (phone && phone.length == 11 && !isNaN(phone)) {
            return true;
        } else {
            return false;
        }
    },
    countdown: function (seckillId, nowTime, startTime, endTime) {
        if (nowTime > endTime) {
            //秒杀已经结束了
            $("#seckill-box").html("秒杀已经结束了");
        } else if (nowTime < startTime) {
            //秒杀还未开始
            var killTime = new Date(startTime + 1000);
            $("#seckill-box").countdown(killTime, function (event) {
                //改动每次时间的变化
                var format = event.strftime('秒杀倒计时：%D天 %H时 %M分 %S秒');
                $("#seckill-box").html(format);
            }).on('finish.countdown', function () {
                seckill.handleSeckill(seckillId, $("#seckill-box"));
            });
        } else {
            seckill.handleSeckill(seckillId, $("#seckill-box"));
        }
    },
    handleSeckill: function (seckillId, node) {
        node.hide().html("<button class='btn btn-primary btn-lg' id='killBtn'>开始秒杀</button>");
        $.post(seckill.URL.exposer(seckillId), {}, function (result) {
            if (result && result.success) {
                var exposer = result.data;
                if (exposer['exposed']) {
                    //开启秒杀
                    var killUrl = seckill.URL.execution(seckillId, exposer['md5']);
                    $("#killBtn").one('click', function () {
                        //执行秒杀请求
                        $(this).addClass("disabled");
                        $.post(killUrl, {}, function (result) {
                            if (result && result['success']) {
                                var killExcution = result['data'];
                                var state = killExcution['state'];
                                var stateInfo = killExcution['stateInfo'];
                                //显示秒杀结果
                                node.html("<span class='label label-success'>" + stateInfo + "</span>");
                            }
                        });
                    });
                    node.show();
                } else {
                    //没有开启秒杀，客户端和服务器的时间没有同步
                    //需要在重新走一遍countdown
                    var now = exposer['now'];
                    var start = exposer['start'];
                    var end = exposer['end'];
                    seckill.countdown(seckillId, now, start, end);
                }
            } else {
                console.log(result);
            }
        });

    },
    //封装详情页面秒杀逻辑
    detail: {
        //详情页初始化
        init: function (params) {
            var killPhone = $.cookie('killPhone');
            var startTime = params['startTime'];
            var endTime = params['endTime'];
            var seckillId = params['seckillId'];

            //验证手机号
            if (!seckill.validatePhone(killPhone)) {
                //绑定手机号
                var killPhoneModal = $("#killPhoneModal");
                killPhoneModal.modal({
                    show: true,//显示弹出层
                    backdrop: 'static',//禁止位置关闭
                    keyboard: false//关闭键盘事件
                });
                $("#killPhoneBtn").click(function () {
                    var inputPhone = $("#killPhoneKey").val();
                    if (seckill.validatePhone(inputPhone)) {
                        //将电话写入到cookie
                        $.cookie('killPhone', inputPhone, {expires: 7, path: '/seckill'});
                        //刷新页面
                        window.location.reload();
                    } else {
                        $("#killPhoneMessage").hide().html('<label class="label label-danger">手机号错误!</label>').show(300);
                    }

                });
            }
            //存在手机号码，开始及时交互
            $.get(seckill.URL.time_now(), {}, function (result) {
                if (result && result.success) {
                    //请求成功
                    var nowTime = result.data;
                    //时间判断
                    seckill.countdown(seckillId, nowTime, startTime, endTime);
                } else {
                    console.log(result);
                }
            });
        }
    }
};