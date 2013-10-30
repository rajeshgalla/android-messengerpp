package org.solovyev.android.messenger.chats;

import com.actionbarsherlock.app.ActionBar;
import org.solovyev.android.fragments.MultiPaneFragmentManager;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.MessagesFragment;
import org.solovyev.android.messenger.users.ContactUiEventType;
import org.solovyev.android.messenger.users.User;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;
import java.util.List;

import static org.solovyev.android.messenger.chats.Chats.CHATS_FRAGMENT_TAG;
import static org.solovyev.android.messenger.messages.MessagesFragment.newMessagesFragmentDef;
import static org.solovyev.android.messenger.users.ContactFragment.newViewContactFragmentDef;
import static org.solovyev.android.messenger.users.ContactsInfoFragment.newViewContactsFragmentDef;
import static org.solovyev.android.messenger.users.Users.showViewUsersFragment;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 1:59 PM
 */
public class ChatUiEventListener implements EventListener<ChatUiEvent> {

	@Nonnull
	private static final String TAG = ChatUiEventListener.class.getSimpleName();

	@Nonnull
	private final BaseFragmentActivity activity;

	@Nonnull
	private final ChatService chatService;

	public ChatUiEventListener(@Nonnull BaseFragmentActivity activity, @Nonnull ChatService chatService) {
		this.activity = activity;
		this.chatService = chatService;
	}

	@Override
	public void onEvent(ChatUiEvent event) {
		final Chat chat = event.getChat();
		final ChatUiEventType type = event.getType();

		switch (type) {
			case open_chat:
				onOpenChatEvent(chat);
				break;
			case chat_clicked:
				onChatClickedEvent(chat);
				break;
			case chat_message_read:
				onMessageReadEvent(chat, event.getDataAsMessage());
				break;
			case show_participants:
				onShowParticipants(chat);
				break;
		}
	}

	private void onShowParticipants(@Nonnull Chat chat) {
		if(chat.isPrivate()) {
			final Entity contactId = chatService.getSecondUser(chat);
			if (contactId != null) {
				final User contact = App.getUserService().getUserById(contactId);
				App.getEventManager(activity).fire(ContactUiEventType.view_contact.newEvent(contact));
			}
		} else {
			final Account account = App.getAccountService().getAccountByEntityOrNull(chat.getEntity());
			if (account != null) {
				final List<User> participants = chatService.getParticipantsExcept(chat.getEntity(), account.getUser().getEntity());
				showViewUsersFragment(participants, activity);
			}
		}
	}

	private void onOpenChatEvent(@Nonnull final Chat chat) {
		final MultiPaneFragmentManager fragmentService = activity.getMultiPaneFragmentManager();
		if (activity.getMultiPaneManager().isDualPane(activity)) {
			if (!fragmentService.isFragmentShown(CHATS_FRAGMENT_TAG)) {
				final ActionBar.Tab tab = activity.findTabByTag(CHATS_FRAGMENT_TAG);
				if (tab != null) {
					tab.select();
				}
			}

			final BaseChatsFragment fragment = fragmentService.getFragment(CHATS_FRAGMENT_TAG);
			if (fragment != null) {
				fragment.clickItemById(chat.getId());
			}
		} else {
			fragmentService.setMainFragment(MessagesFragment.newMessagesFragmentDef(activity, chat, true));
		}
	}

	private void onMessageReadEvent(@Nonnull Chat chat, @Nonnull Message message) {
		chatService.onMessageRead(chat, message);
	}

	private void onChatClickedEvent(@Nonnull final Chat chat) {
		final MessengerMultiPaneFragmentManager fm = activity.getMultiPaneFragmentManager();

		if (activity.isDualPane()) {
			fm.clearBackStack();
			fm.setSecondFragment(newMessagesFragmentDef(activity, chat, false));
			if (activity.isTriplePane()) {
				try {
					final Account account = activity.getAccountService().getAccountByEntity(chat.getEntity());

					if (chat.isPrivate()) {
						fm.setThirdFragment(newViewContactFragmentDef(activity, account, chat.getSecondUser(), false));
					} else {
						final List<User> participants = activity.getChatService().getParticipantsExcept(chat.getEntity(), account.getUser().getEntity());
						fm.setThirdFragment(newViewContactsFragmentDef(activity, participants, false));
					}
				} catch (UnsupportedAccountException e) {
					App.getExceptionHandler().handleException(e);
				}
			}

		} else {
			fm.setMainFragment(newMessagesFragmentDef(activity, chat, true));
		}
	}
}
