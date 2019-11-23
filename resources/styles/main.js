// Global variable to hold the websocket.
var socket = null;

/**
 * This function is in charge of connecting the client.
 */
function connect() {

    console.log("Begin connect");
    socket = new WebSocket("ws://" + window.location.host + "/ws");


    socket.onerror = function() {
        console.log("socket error");
    };


    socket.onopen = function() {
   //     write("Connected");
    };

    socket.onclose = function(evt) {
        var explanation = "";
        if (evt.reason && evt.reason.length > 0) {
            explanation = "reason: " + evt.reason;
        } else {
            explanation = "without a reason specified" ;
        }

      //  write("Disconnected with close code " + evt.code + " and " + explanation);
        setTimeout(connect, 5000);
    };

    // If we receive a message from the server, we want to handle it.
    socket.onmessage = function(event) {
        console.log(event.data);
        let responseData = JSON.parse(event.data)
        if (responseData.message == "failure")
            return;

        if (responseData != null ){
            switch (responseData.command){
                case "server-add-friend":
                    $("#friend-request-sent-list")
                        .append('<li class="media" data-id="sent-request-'+responseData.message+'><h5>'+responseData.message+'</h5>');
                    break;

                case "server-friend-request":
                    $("#friend-request-waiting-list")
                        .append('<li class="media" data-id="waiting-request-'+responseData.message+'"><h5>'+responseData.message+'</h5>'+
                        '<button class="btn pure-button-primary accept-friend-btn" data-id="'+responseData.message+'" type="button">Accept</button></li>');
                    $('.accept-friend-btn').click(onAcceptFriendRequest);
                    break;

                case "server-accept-request":
                    $("#friend-list")
                        .append('<li class="media"><a class="chat-friend-btn" data-id="'+responseData.message+'">'+responseData.message+'</a></li>');
                    $('li.[data-id="sent-request-'+responseData.message+'"]').remove();
                    break;

                case "server-accept-friend":
                    $("#friend-list")
                        .append('<li class="media"><a class="chat-friend-btn" data-id="'+responseData.message+'">'+responseData.message+'</a></li>');
                    $('li.[data-id="waiting-request-'+responseData.message+'"]').remove();
                    break;
            }
        }

    };
}

function onSendFriendRequest() {
    var input = document.getElementById("add-friend-email");

    if (input) {
        var email = input.value;
        if (email && socket) {
            socket.send('{command : "client-add-friend", message : "'+ email +'"}');
            input.value = "";
        }
    }
}

function onAcceptFriendRequest(event){
    event.preventDefault();
    let email= $(this).attr('data-id');
    if (email && socket) {
        socket.send('{command : "client-accept-friend", message : "'+ email +'"}');
    }
}

$(document).ready(function() {
    connect();

    $("#add-friend-btn").click(onSendFriendRequest);
    $(".accept-friend-btn").click(onAcceptFriendRequest);

    var chatList = document.getElementById("chat-message-list");
    chatList.scrollTop = chatList.scrollHeight;
});
