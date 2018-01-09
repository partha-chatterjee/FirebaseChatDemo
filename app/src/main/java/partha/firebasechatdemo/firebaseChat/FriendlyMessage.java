/**
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package partha.firebasechatdemo.firebaseChat;

public class FriendlyMessage {

    private String text;
    private String name;
    private String senderPhotoUrl;
    private String imageUrl;
    private String msgTime;
    private String sender_id;
    private String receiver_id;
    String type="";
    String msgstatus="";
    private String msgID;
    private Boolean isEdit;


    public FriendlyMessage() {
    }

    public FriendlyMessage(String text, String name, String photoUrl, String imageUrl) {
        this.text = text;
        this.name = name;
        this.senderPhotoUrl = photoUrl;
        this.imageUrl = imageUrl;
    }

    public FriendlyMessage(String senderId, String receiverId, String text, String name, String photoUrl, String imageUrl, String msgTime, Boolean isEdit) {
        this.sender_id = senderId;
        this.receiver_id=receiverId;
        this.text = text;
        this.name = name;
        this.senderPhotoUrl = photoUrl;
        this.imageUrl = imageUrl;
        this.msgTime = msgTime;
        this.isEdit = isEdit;
    }

    public FriendlyMessage(String senderId, String receiverId, String text, String name, String photoUrl, String imageUrl, String msgTime, String msgID) {
        this.sender_id = senderId;
        this.receiver_id=receiverId;
        this.text = text;
        this.name = name;
        this.senderPhotoUrl = photoUrl;
        this.imageUrl = imageUrl;
        this.msgTime = msgTime;
        this.msgID = msgID;
    }

    public String getsender_id() {
        return sender_id;
    }

    public void setsender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getreceiver_id() {
        return receiver_id;
    }

    public void setreceiver_id(String receiver_id) {
        this.receiver_id = receiver_id;
    }

    public String getmessage() {
        return text;
    }

    public void setmessage(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSenderPhotoUrl() {
        return senderPhotoUrl;
    }

    public void setSenderPhotoUrl(String senderPhotoUrl) {
        this.senderPhotoUrl = senderPhotoUrl;
    }

    public String getimage_url() {
        return imageUrl;
    }

    public void setimage_url(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String gettype() {
        return type;
    }

    public void settype(String type) {
        this.type = type;
    }

    public String getsenddate() {
        return msgTime;
    }

    public void setsenddate(String msgTime) {
        this.msgTime = msgTime;
    }

    public String getmsgstatus() {
        return msgstatus;
    }

    public void setmsgstatus(String msgstatus) {
        this.msgstatus = msgstatus;
    }

    public String getMsgID() {
        return msgID;
    }

    public void setMsgID(String msgID) {
        this.msgID = msgID;
    }

    public Boolean getEdit() {
        return isEdit;
    }

    public void setEdit(Boolean edit) {
        isEdit = edit;
    }
}
