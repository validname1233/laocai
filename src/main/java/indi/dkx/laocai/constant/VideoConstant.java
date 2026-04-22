package indi.dkx.laocai.constant;

import java.io.File;

public interface VideoConstant {
    String VIDEO_DOWNLOAD_ROOT_DIR = System.getProperty("user.dir")
            + File.separator + "tmp"
            + File.separator + "videos"
            + File.separator;
    String COOKIE_ROOT_DIR = System.getProperty("user.dir") + File.separator ;
}
