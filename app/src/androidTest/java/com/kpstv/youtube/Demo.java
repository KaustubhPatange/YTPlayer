


//<iframe width="100%" height="400" scrolling="no" frameborder="no" src="https://w.soundcloud.com/player/?visual=true&url=https://api.soundcloud.com/tracks/212263577&show_artwork=true"></iframe>

package com.kpstv.youtube;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Demo {

    void c() {
      Document document = Jsoup.parse("<iframe width=\"100%\" height=\"400\" scrolling=\"no\" frameborder=\"no\" src=\"https://w.soundcloud.com/player/?visual=true&url=https://api.soundcloud.com/tracks/212263577&show_artwork=true\"></iframe>");
        document.getElementsByAttribute("src");
    }
}
