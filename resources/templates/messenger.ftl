<#-- @ftlvariable name="userId" type="java.lang.String" -->

<#import "template.ftl" as layout />

<@layout.mainLayout title="Messenger">
    <div class="col-md-8 h-100">
        <div class="panel h-100">
            <div class="panel-heading" id="chat-header">
                RECENT CHAT HISTORY
            </div>
            <div class="panel-body chat-content" id="chat-panel-body">
                <ul class="media-list" id= "chat-message-list">
                <!--
                    <#list 1..10 as x>
                        <li class="media message my-message">
                            <p>Donec sit amet ligula enim. Duis vel condimentum massa.
                               Donec sit amet ligula enim. Duis vel condimentum massa.Donec sit amet ligula enim.
                               Duis vel condimentum massa.
                               Donec sit amet ligula enim. Duis vel condimentum massa.</p>

                        </li>
                        <li class="media message their-message">
                            <p>Donec sit amet ligula enim. Duis vel condimentum massa.
                               Donec sit amet ligula enim. Duis vel condimentum massa.Donec sit amet ligula enim.
                               Duis vel condimentum massa.
                               Donec sit amet ligula enim. Duis vel condimentum massa.</p>

                        </li>
                    </#list>
                -->
                </ul>
            </div>
            <div class="panel-footer">
                <div class="input-group">
                    <input type="text" class="form-control" id="message-text-field" maxlength="900" placeholder="Enter Message" disabled/>
                    <span class="input-group-btn">
                        <button class="btn pure-button-primary" type="button" id="send-message-btn" disabled>SEND</button>
                    </span>
                </div>
            </div>
        </div>
    </div>

    <div class="col-md-4 h-100">

        <div class="panel">
            <div class="panel-heading">
               FRIEND LIST
            </div>
            <div class="panel-body">
                <ul id="friend-list" class="media-list">
                   <#list friendList as friend>
                       <li class="media">
                           <a class="chat-friend-btn" href="#" data-id="${friend.userId}">${friend.email}</a>
                       </li>
                   </#list>
                </ul>
            </div>
        </div>

        <div class="panel">
            <div class="panel-heading">
               PENDING REQUEST(S)
            </div>
            <div class="panel-body">
                <ul id="friend-request-waiting-list" class="media-list">
                    <#list friendRequestWaitingList as friendRequest>
                        <li class="media" id="waiting-request-${friendRequest.userId}">
                            <h5>${friendRequest.email}</h5>
                            <button class="btn pure-button-primary accept-friend-btn" data-id="${friendRequest.email}" type="button">Accept</button>
                        </li>
                    </#list>
                </ul>
            </div>
        </div>

        <div class="panel">
            <div class="panel-heading">
               SENT REQUEST(S)
            </div>
            <div class="panel-body">
                <ul id="friend-request-sent-list" class="media-list">
                    <#list friendRequestSentList as friendRequest>
                        <li class="media" id="sent-request-${friendRequest.userId}">
                            <h5>${friendRequest.email}</h5>
                        </li>
                    </#list>
                </ul>
            </div>
            <div class="panel-footer">
                <div class="input-group">
                    <input id="add-friend-email" type="text" class="form-control" maxlength="100" placeholder="Enter Email" />
                    <span class="input-group-btn">
                        <button class="btn pure-button-primary" id="add-friend-btn" type="button">ADD</button>
                    </span>
                </div>
            </div>
        </div>

    </div>

    <#if user??>
        <script src="/styles/main.js"></script>
    </#if>
</@layout.mainLayout>
