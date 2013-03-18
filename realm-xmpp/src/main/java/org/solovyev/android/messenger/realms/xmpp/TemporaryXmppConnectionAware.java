package org.solovyev.android.messenger.realms.xmpp;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/6/13
 * Time: 10:19 PM
 */

/**
 * Temporary class which is used when realm xmpp connection is not established yet
 */
class TemporaryXmppConnectionAware implements XmppConnectionAware {

    @Nonnull
    private final XmppRealm realm;

    private TemporaryXmppConnectionAware(@Nonnull XmppRealm realm) {
        this.realm = realm;
    }

    @Nonnull
    static XmppConnectionAware newInstance(@Nonnull XmppRealm realm) {
        return new TemporaryXmppConnectionAware(realm);
    }

    @Override
    public <R> R doOnConnection(@Nonnull XmppConnectedCallable<R> callable) throws XMPPException {
        final Connection connection = new XMPPConnection(realm.getConfiguration().toXmppConfiguration());
        XmppRealmConnection.checkConnectionStatus(connection, realm);
        return callable.call(connection);
    }
}
