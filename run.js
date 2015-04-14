try {
    require("source-map-support").install();
} catch(err) {
}
require("./out/goog/bootstrap/nodejs.js");
require("./out/delta_bot.js");
goog.require("delta_bot.core");
goog.require("cljs.nodejscli");
