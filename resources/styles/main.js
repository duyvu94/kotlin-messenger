// Global variable to hold the websocket.
var socket = null;

function connect() {
    $('body').loadingModal({ text: 'Connecting...' });

    socket = new WebSocket("ws://" + window.location.host + "/ws");

    socket.onerror = function() {
        $('body').loadingModal({ text: 'Reconnecting...' });
        console.log("socket error");
    };


    socket.onopen = function() {
        console.log("Connected");
        $('body').loadingModal('hide');
    };

    socket.onclose = function(evt) {
        $('body').loadingModal({ text: 'Reconnecting...' });

        setTimeout(connect, 5000);
    };

    // If we receive a message from the server, we want to handle it.
    socket.onmessage = function(event) {
        let responseData = JSON.parse(event.data);
        console.log(responseData);
        if (responseData.message == "failure")
            return;

        if (responseData != null ){
            switch (responseData.command){
                case "server-add-friend":
                    $("#friend-request-sent-list")
                        .append('<li class="media" id="sent-request-'+responseData.extraMessage+'"><h5>'+responseData.message+'</h5></li>');
                    break;

                case "server-friend-request":
                    $("#friend-request-waiting-list")
                        .append('<li class="media" id="waiting-request-'+responseData.extraMessage+'"><h5>'+responseData.message+'</h5>'+
                        '<button class="btn pure-button-primary accept-friend-btn" data-id="'+responseData.message+'" type="button">Accept</button></li>');
                    $('.accept-friend-btn').click(onAcceptFriendRequest);
                    break;

                case "server-accept-request":
                    $("#friend-list")
                        .append('<li class="media"><a class="chat-friend-btn" href="#" data-id="'+responseData.extraMessage+'">'+responseData.message+'</a></li>');
                    $('.chat-friend-btn').click(onStartChat);
                    $("#sent-request-"+responseData.extraMessage).remove();
                    break;

                case "server-accept-friend":
                    $("#friend-list")
                        .append('<li class="media"><a class="chat-friend-btn" href="#" data-id="'+responseData.extraMessage+'">'+responseData.message+'</a></li>');
                    $('.chat-friend-btn').click(onStartChat);
                    $("#waiting-request-"+responseData.extraMessage).remove();
                    break;

                case 'server-send-message':
                    if ($('#chat-message-list').attr('data-id') != responseData.message)
                        break;
                    $('#chat-message-list').append('<li class="media message my-message"><p>'+unescape(responseData.extraMessage)+'</p><small>'+responseData.date+'</small></li>');
                    $('#chat-panel-body').scrollTop($('#chat-message-list').height());
                    break;

                case 'server-receive-message':
                    if ($('#chat-message-list').attr('data-id') != responseData.message)
                        break;
                    $('#chat-message-list').append('<li class="media message their-message"><p>'+unescape(responseData.extraMessage)+'</p><small>'+responseData.date+'</small></li>');
                    $('#chat-panel-body').scrollTop($('#chat-message-list').height());
                    break;
            }
        }

    };
}

function onStartChat(e){
    e.preventDefault();
    let id = $(this).attr('data-id');
    if (id && socket) {
        $('#chat-header').html('RECENT CHAT HISTORY ('+ $(this).html() +')');
        $('#chat-message-list').empty();
        $('#chat-message-list').attr('data-id', id);
        $('#send-message-btn').prop('disabled', false);
        $('#message-text-field').prop('disabled', false);
        $('#message-text-field').val('');
        socket.send('{command : "client-chat-history", message : "'+ id +'", extraMessage: "" }');
    }
}

function onSendFriendRequest() {
    var input = document.getElementById("add-friend-email");

    if (input) {
        var email = input.value;
        if (email && socket) {
            socket.send('{command : "client-add-friend", message : "'+ email +'", extraMessage: "" }');
            input.value = "";
        }
    }
}

function onAcceptFriendRequest(){
    let email= $(this).attr('data-id');
    if (email && socket) {
        socket.send('{ command : "client-accept-friend", message : "'+ email +'", extraMessage: "" }');
    }
}

function onSendMessage(){
    let message= $('#message-text-field').val();
    let receiver = $('#chat-message-list').attr('data-id');
    if (message && message.length < 1000 && receiver && socket) {
        var jsonObj = JSON.parse( '{ "command" : "client-send-message", "message" : "'+ receiver +'" }' );
        jsonObj.extraMessage = escape(message);
        socket.send(JSON.stringify(jsonObj));
        $('#message-text-field').val('');
    }
}

$(document).ready(function() {
    connect();

    $('#add-friend-btn').click(onSendFriendRequest);
    $('.accept-friend-btn').click(onAcceptFriendRequest);
    $('#send-message-btn').click(onSendMessage);
    $('.chat-friend-btn').click(onStartChat);

    $('#message-text-field').keypress(function(e) {
        var key = e.which;
         if(key == 13)  // the enter key code
          {
            $('#send-message-btn').click();
            return false;
          }
    })

});
