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
        write("Connected");
    };

    socket.onclose = function(evt) {
        var explanation = "";
        if (evt.reason && evt.reason.length > 0) {
            explanation = "reason: " + evt.reason;
        } else {
            explanation = "without a reason specified" ;
        }

        write("Disconnected with close code " + evt.code + " and " + explanation);
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
                    $("#friend-request-sent-list").append('<li class="media">'+responseData.message+'</li>')
                    break;

                case "server-friend-request":
                    $("#friend-request-waiting-list").append('<li class="media">'+responseData.message+'</li>')
                    break;
            }
        }

    };
}


function write(message) {

    var line = document.createElement("p");
    line.className = "message";
    line.textContent = message;

    // Then we get the 'messages' container that should be available in the HTML itself already.
    var messagesDiv = document.getElementById("messages");
    // We adds the text
    //messagesDiv.appendChild(line);
    // We scroll the container to where this text is so the use can see it on long conversations if he/she has scrolled up.
    //messagesDiv.scrollTop = line.offsetTop;
}

/**
 * Function in charge of sending the 'commandInput' text to the server via the socket.
 */
function onSend() {
    socket.send("{command : 'abc', message : 'def'}");
    var input = document.getElementById("commandInput");
    // Validates that the input exists
    if (input) {
        var text = input.value;
        // Validates that there is a text and that the socket exists
        if (text && socket) {
            // Sends the text
            socket.send("{command : 'abc', message : 'def'}");
            // Clears the input so the user can type a new command or text to say
            input.value = "";
        }
    }
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

/**
 * The initial code to be executed once the page has been loaded and is ready.
 */
function start() {
    // First, we should connect to the server.
    connect();

    // If we click the sendButton, let's send the message.
    document.getElementById("add-friend-btn").onclick = onSendFriendRequest;


    var chatList = document.getElementById("chat-message-list");
    chatList.scrollTop = chatList.scrollHeight;
    // If we pressed the 'enter' key being inside the 'commandInput', send the message to improve accessibility and making it nicer.
    /* document.getElementById("commandInput").onkeydown = function(e) {
        if (e.keyCode == 13) {
            onSend();
        }
    }; */
}

/**
 * The entry point of the client.
 */
function initLoop() {
    // Is the sendButton available already? If so, start. If not, let's wait a bit and rerun this.
    if (document.getElementById("add-friend-btn")) {
        start();
    } else {
        setTimeout(initLoop, 300);
    }
}

// This is the entry point of the client.
initLoop();
