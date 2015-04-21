# Delta-bot

Run a command over and over again, and be notified when something changes.

This bot runs on Node.js and currently connects to hipchat (and possibly other Jabber/XMPP servers).

### HipChat
For your HipChat jabber id, go here: https://hipchat.com/account/xmpp

    :: Send to yourself
    node run.js pwd "<your pwd here>" my-jid XXXXXX_XXXXXX@chat.hipchat.com server "chat.hipchat.com" cmd "sqlcmd -E -i myquery.sql" server "chat.hipchat.com"
    :: Send to someone else
    node run.js pwd "<your pwd here>" my-jid XXXXXX_XXXXXX@chat.hipchat.com to-jid XXXXXX_XXXXXX@chat.hipchat.com cmd "sqlcmd -E -i myquery.sql" server "chat.hipchat.com"
    :: Send to a room
    node run.js pwd "<your pwd here>" my-jid XXXXXX_XXXXXX@chat.hipchat.com my-nick "My Name" room-jid XXXXXX_room_name@conf.hipchat.com cmd "sqlcmd -E -i myquery.sql" server "chat.hipchat.com"

You can specify `my-jid` in `%JABBER_ID%`, `pwd` in `%JABBER_PWD%`, and `server` in `%JABBER_SERVER%`.

## Development
This bot is written in ClojureScript for Node.js.  Feel free to make pull requests, issues, feature requests, etc..  This is a type project, at the moment, but let me know if you have interest in making this big.

## License
[ISC](LICENSE)
