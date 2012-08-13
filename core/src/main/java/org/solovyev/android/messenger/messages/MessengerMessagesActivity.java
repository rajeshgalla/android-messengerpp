package org.solovyev.android.messenger.messages;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.widget.TextView;
import com.actionbarsherlock.view.MenuItem;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.AndroidUtils;
import org.solovyev.android.http.RemoteFileService;
import org.solovyev.android.messenger.MessengerFragmentActivity;
import org.solovyev.android.messenger.R;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatListItem;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserEventListener;
import org.solovyev.android.messenger.users.UserEventType;

import java.util.List;

/**
 * User: serso
 * Date: 6/9/12
 * Time: 10:42 PM
 */
public class MessengerMessagesActivity extends MessengerFragmentActivity implements UserEventListener {

    @Inject
    @NotNull
    private RemoteFileService remoteFileService;

    @NotNull
    private static final String CHAT_ID = "chat_id";

    public static void startActivity(@NotNull Activity activity, @NotNull Chat chat) {
        final Intent result = new Intent();
        result.setClass(activity, MessengerMessagesActivity.class);
        result.putExtra(CHAT_ID, chat.getId());
        activity.startActivity(result);
    }

    private int pagerPosition = 0;

    @Nullable
    private ViewPager pager;

    @NotNull
    private Chat chat;

    @Nullable
    private User contact;

    public MessengerMessagesActivity() {
        super(R.layout.msg_main, false, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = this.getIntent();
        if (intent != null) {
            final String chatId = intent.getExtras().getString(CHAT_ID);
            if (chatId != null) {
                final Chat chatFromService = getChatService().getChatById(chatId, this);
                if (chatFromService != null) {
                    this.chat = chatFromService;
                } else {
                    this.finish();
                }
            } else {
                this.finish();
            }
        } else {
            this.finish();
        }

        getUserService().addUserEventListener(this);

        final List<User> participants = getChatService().getParticipantsExcept(chat.getId(), getUser().getId(), this);
        if (chat.isPrivate()) {
            if (!participants.isEmpty()) {
                contact = participants.get(0);
            }
        }

        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        final MessengerMessagesFragment fragment = new MessengerMessagesFragment(chat);
        fragmentTransaction.add(R.id.content, fragment);
        fragmentTransaction.commit();

        setTitle(createTitle());

        /*final MessagesFragmentPagerAdapter adapter = new MessagesFragmentPagerAdapter(getSupportFragmentManager(),
                getString(R.string.c_messages), chat);

        pager = initTitleForViewPager(this, this, adapter);

        final ImageButton attachButton = createFooterImageButton(R.drawable.msg_attach_icon, R.string.c_attach);
        getFooterLeft().addView(attachButton, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        final EditText messageBox = ViewFromLayoutBuilder.<EditText>newInstance(R.layout.msg_message_box).build(this);
        getFooterCenter().addView(messageBox, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        final Button sendButton = createFooterButton(R.string.c_send);
        getFooterRight().addView(sendButton, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String messageText = StringUtils2.toHtml(messageBox.getText());

                if (!StringUtils.isEmpty(messageText)) {
                    Toast.makeText(MessengerMessagesActivity.this, "Sending...", Toast.LENGTH_SHORT).show();

                    new SendMessageAsyncTask(MessengerMessagesActivity.this, chat) {
                        @Override
                        protected void onSuccessPostExecute(@Nullable List<ChatMessage> result) {
                            super.onSuccessPostExecute(result);
                            messageBox.setText("");
                        }
                    }.execute(new SendMessageAsyncTask.Input(getUser(), messageText, chat));
                }
            }
        });

        final ImageButton backButton = createFooterImageButton(R.drawable.msg_back, R.string.c_back);
        getHeaderLeft().addView(backButton, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessengerMessagesActivity.this.finish();
            }
        });

        final View headerCenterView = ViewFromLayoutBuilder.newInstance(R.layout.msg_message_header_title).build(this);

        if (AndroidUtils.getScreenOrientation(this) != Configuration.ORIENTATION_LANDSCAPE) {
            // message title
            final TextView messageTitle = (TextView) headerCenterView.findViewById(R.id.message_header_title);
            messageTitle.setText(createTitle());
        }
        getHeaderCenter().addView(headerCenterView);

        if ( contact != null ) {
            setTitle(createTitle());
        }

        // online icon
        if (contact != null) {
            changeOnlineStatus(contact.isOnline());
        }

        // contact icon
        if (contact != null) {
            final ImageView contactIcon = createFooterImageButton(R.drawable.empty_icon, R.string.c_contact);

            final String imageUri = contact.getPropertyValueByName("photo");
            if (!StringUtils.isEmpty(imageUri)) {
                remoteFileService.loadImage(imageUri, contactIcon, R.drawable.empty_icon);
            }

            getHeaderRight().addView(contactIcon, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }*/
    }

    private String createTitle() {
        return ChatListItem.getDisplayName(chat, getChatService().getLastMessage(chat.getId(), this), getUser());
    }

    private void changeOnlineStatus(boolean online) {
        if (AndroidUtils.getScreenOrientation(this) != Configuration.ORIENTATION_LANDSCAPE) {
            final TextView contactOnline = (TextView) getHeaderCenter().findViewById(R.id.contact_online);
            if (online) {
                contactOnline.setText("·");
            } else {
                contactOnline.setText("");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        getUserService().removeUserEventListener(this);
    }

    @Override
    public void onUserEvent(@NotNull final User eventUser, @NotNull final UserEventType userEventType, @Nullable final Object data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (contact != null) {
                    if (userEventType == UserEventType.contact_online) {
                        final User eventContact = (User) data;
                        if (contact.equals(eventContact)) {
                            changeOnlineStatus(true);
                            contact = eventContact;
                        }
                    }

                    if (userEventType == UserEventType.contact_offline) {
                        final User eventContact = (User) data;
                        if (contact.equals(eventContact)) {
                            changeOnlineStatus(false);
                            contact = eventContact;
                        }
                    }

                    if (userEventType == UserEventType.changed) {
                        if (eventUser.equals(contact)) {
                            contact = eventUser;
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return false;
    }
}
