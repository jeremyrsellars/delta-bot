# Delta-bot

Run a command over and over again, and be notified when something changes.

This bot runs on Node.js and currently connects to hipchat (and possibly other Jabber/XMPP servers).

### HipChat
For your HipChat jabber id, go here: https://hipchat.com/account/xmpp

    node run.js pwd "<your pwd here>" jid XXXXXX_XXXXXX@chat.hipchat.com cmd "sqlcmd -E -i myquery.sql"

## Development
This bot is written in ClojureScript for Node.js.  Feel free to make pull requests, issues, feature requests, etc..  This is a type project, at the moment, but let me know if you have interest in making this big.

## License
[ISC](LICENSE)
