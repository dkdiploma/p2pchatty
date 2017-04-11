var videos = document.getElementById("videos");
var sendRTCComment = $('.sendInstant');
var msgs = document.getElementById("msgs");
var sendFileBtn = document.getElementById("sendFileRTCBtn");
var files = document.getElementById("files");
var rtc = SkyRTC();


function myObjectType(property1, property2) {
    this.name = property1,
            this.text = property2;
}
// now create a new instance
/**********************************************************/
sendRTCComment.submit(function(event) {
    var msgIpt = document.getElementById("newMessageText"),
            msg = new myObjectType(document.getElementById("userName").value, msgIpt.value),
            p = document.createElement("p");
    p.innerText = "me: " + msg.text;
    rtc.broadcast(msg);
    msgIpt.value = "";
    msgs.appendChild(p);
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
    if (window.confirm("Do you want to receive" + fileName + "from" +socketId +"which size is"+ fileSize + "KB？")) {
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
        "audio": true
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
    p.innerText = message.name + ": " + message.text;
    msgs.appendChild(p);
});
