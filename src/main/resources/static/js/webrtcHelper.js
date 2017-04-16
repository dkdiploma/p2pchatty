var videos = document.getElementById("videos");
var sendRTCComment = $('.sendInstant');
var msgs = document.getElementById("msgs");
var sendFileBtn = document.getElementById("sendFileRTCBtn");
var files = document.getElementById("files");
var rtc = SkyRTC();


function channelMessage(name, userId, date, messageContent, avatarImage) {
    this.name = name;
    this.userId = userId;
    this.date = date;
    this.messageContent = messageContent;
    this.avatarImage = avatarImage;
}
// now create a new instance
/**********************************************************/
sendRTCComment.submit(function (event) {
    var msgIpt = document.getElementById("newMessageText"),
            msg = new channelMessage(document.getElementById("userName").value, $('#current-user-id').val(),
                    new Date(), msgIpt.value, document.getElementById("avatarImage").value),
            p = document.createElement("p");
    rtc.broadcast(msg);
    instantCommentHandler(msg);
    msgIpt.value = "";
    return false;
});

sendFileBtn.onclick = function (event) {

    rtc.shareFile("fileIpt");
};
/**********************************************************/


rtc.on("send_file_accepted", function (sendId, socketId, file) {
    var p = document.getElementById("sf-" + sendId);
    p.innerText = "File received:" + file.name;

});

rtc.on("send_file_refused", function (sendId, socketId, file) {
    var p = document.getElementById("sf-" + sendId);
    p.innerText = "File acceptance refused" + file.name;
});

rtc.on('send_file', function (sendId, socketId, file) {
    var p = document.createElement("p");
    p.innerText = "Send " + file.name;
    p.id = "sf-" + sendId;
    files.appendChild(p);
});

rtc.on('sended_file', function (sendId, socketId, file) {
    var p = document.getElementById("sf-" + sendId);
    p.parentNode.removeChild(p);
});

rtc.on('send_file_chunk', function (sendId, socketId, percent, file) {
    var p = document.getElementById("sf-" + sendId);
    p.innerText = file.name + "Process: " + Math.ceil(percent) + "%";
});
rtc.on('receive_file_chunk', function (sendId, socketId, fileName, percent) {
    var p = document.getElementById("rf-" + sendId);
    p.innerText = "Processing " + fileName + "Result：" + Math.ceil(percent) + "%";
});

rtc.on('receive_file', function (sendId, socketId, name) {
    var p = document.getElementById("rf-" + sendId);
    p.parentNode.removeChild(p);
});

rtc.on('send_file_error', function (error) {
    console.log(error);
});

rtc.on('receive_file_error', function (error) {
    console.log(error);
});

rtc.on('receive_file_ask', function (sendId, socketId, fileName, fileSize) {
    var p;
    if (window.confirm("Do you want to receive" + fileName + "from" + socketId + "which size is" + fileSize + "KB？")) {
        rtc.sendFileAccept(sendId);
        p = document.createElement("p");
        p.innerText = "File name:" + fileName;
        p.id = "rf-" + sendId;
        files.appendChild(p);
    } else {
        rtc.sendFileRefuse(sendId);
    }
});

rtc.on("connected", function () {
//imitation
    rtc.createStream({
        "video": false,
        "audio": false
    });
});

rtc.on("stream_created", function (stream) {
    document.getElementById('me').src = URL.createObjectURL(stream);
    // document.getElementById('me').play();
});

rtc.on("stream_create_error", function () {
    alert("create stream failed!");
});

rtc.on('pc_add_stream', function (stream, socketId) {
    var newVideo = document.createElement("video"),
            id = "other-" + socketId;
    newVideo.setAttribute("class", "other");
    newVideo.setAttribute("autoplay", "autoplay");
    newVideo.setAttribute("id", id);
    videos.appendChild(newVideo);
    rtc.attachStream(stream, id);
});

rtc.on('remove_peer', function (socketId) {
    var video = document.getElementById('other-' + socketId);
    if (video) {
        video.parentNode.removeChild(video);
    }
});

rtc.on('data_channel_message', function (channel, socketId, message) {
    var p = document.createElement("p");
    instantCommentHandler(message);
    //  p.innerText = message.name + ": " + message.text;
    //msgs.appendChild(p);
});

instantCommentHandler = function (obj) {
    //   var obj = JSON.parse(resp);
    console.log("get info: " + obj);
    var storage = null;
    var template = null;
    template = $("#templateMainComment").clone();
    template.find("#templateComment").removeAttr("style");
    template.find("#templateComment").removeAttr("id");
    template.removeAttr("style");
    template.removeAttr("id");
    storage = $(".comments-list");
    template.prependTo(storage);
    if ($('#current-user-id').val() != obj.userId) {
        showModal(obj.name + ": \"" + obj.messageContent + "\"", $('#new-comment-header').val(), "blue");
    }
    changeContentOfInstantComment(template, obj);
    var commentCounter = $("#commentCounter");
    commentCounter.text(+commentCounter.text() + 1);
}

changeContentOfInstantComment = function (cont, obj) {
    var mediaBody = $(cont).find("div[class='media-body']").first();
    var mediaLeft = $(cont).find("div[class='media-left']").first();
    $(mediaLeft).find('img')[0].src = obj.avatarImage;
//    $(cont).find("ul").attr("id", obj.messageId);
//    $(mediaBody).find("form[action='/profile']")
//            .attr("id", "comment_form" + obj.messageId);
    $(mediaBody).find("form[action='/profile']")
            .find("input[name='id']")
            .attr("value", obj.userId);
    $(mediaBody).find("form[action='/profile']")
            .find("a").text(obj.name);
    if (obj.toWhom != null) {
        $(mediaBody).find("form[action='/profile']")
                .find("small").text('To ' + obj.toWhom).addClass("embedenceShowToWhom");
    }
    //onclick, username

    $(mediaBody).find("p").first().text(obj.messageContent)
    var lastDiv = $(cont).find("div[class='last-div']").first();
    //   $(lastDiv).attr("id", "reply" + obj.messageId);
    //  $(lastDiv).find("input[name='id']").attr("value", obj.messageId);
    $(lastDiv).find("form").submit(function (event) {
        Interactive.sendComment(event.target);
        return false;
    });
    $(mediaBody).find("small").remove();
//    $(mediaBody).find("small").find("a").click(function () {
//        $(lastDiv).toggle();
//    });
    var newreplyForm = $(lastDiv).find("form[action='/newreply']").first();
    newreplyForm.remove();
    newreplyForm.parent().remove();
    var voteFor = $(mediaBody).find("form[action='/comment/voteFor']");
    voteFor.remove();
    var voteAgainst = $(mediaBody).find("form[action='/comment/voteAgainst']");
    voteAgainst.remove();

}
