<#-- @ftlvariable name="userId" type="java.lang.String" -->

<#import "template.ftl" as layout />

<@layout.mainLayout title="Messenger">
    <div class="col-md-8 h-100">
        <div class="panel h-100">
            <div class="panel-heading">
                RECENT CHAT HISTORY
            </div>
            <div class="panel-body chat-content">
                <ul class="media-list">
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
                </ul>
            </div>
            <div class="panel-footer">
                <div class="input-group">
                    <input type="text" class="form-control" placeholder="Enter Message" />
                    <span class="input-group-btn">
                        <button class="btn pure-button-primary" type="button">SEND</button>
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
                <ul class="media-list">
                    <#list 1..5 as x>
                        <li class="media">
                            <h5>Alex Deo | User </h5>
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
                <ul class="media-list">
                    <#list 1..5 as x>
                        <li class="media">
                            <h5>Alex Deo | User </h5>
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
                <ul class="media-list">
                    <#list 1..5 as x>
                        <li class="media">
                            <h5>Alex Deo | User </h5>
                        </li>
                    </#list>
                </ul>
            </div>
            <div class="panel-footer">
                <div class="input-group">
                    <input type="text" class="form-control" placeholder="Enter Email" />
                    <span class="input-group-btn">
                        <button class="btn pure-button-primary" id="add-friend-btn" type="button">ADD</button>
                    </span>
                </div>
            </div>
        </div>

    </div>
</@layout.mainLayout>
