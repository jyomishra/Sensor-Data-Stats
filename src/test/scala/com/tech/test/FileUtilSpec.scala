package com.tech.test

import org.scalatest.{FunSuite, Matchers, WordSpecLike}
import os.{Path, pwd}

class FileUtilSpec extends WordSpecLike with Matchers {

  val fileUtil = new FileUtil

  "FileUtil" should {
    "read directory from relative path" in {
      val fileList = fileUtil.getCsvFileList("src\\main\\resources\\csv")
      fileList.size shouldBe 4
    }

    "read directory from absolute path" in {
      val absolutePath = pwd / "src\\main\\resources\\csv"
      println(absolutePath)
      val fileList = fileUtil.getCsvFileList(absolutePath.toString())
      fileList.size shouldBe 4
    }
  }
}
