package com.kpstv.youtube.utils

import java.text.SimpleDateFormat
import java.util.*


fun Date.getFormattedDate() =
        SimpleDateFormat("yyyyMMddHHmmss").format(this)