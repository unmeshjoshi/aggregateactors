package com.moviebooking.writeside.integration

import akka.stream.alpakka.ftp.FtpSettings

class AlpakkaFileApp {
  import akka.stream.alpakka.ftp.FtpCredentials.AnonFtpCredentials
  import java.net.InetAddress

  val settings = FtpSettings(
    InetAddress.getByName("localhost"),
    credentials = AnonFtpCredentials,
    binary = true,
    passiveMode = true
  )

}
