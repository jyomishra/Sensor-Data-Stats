package com.tech.test

import os.{Path, pwd}

class FileUtil {

  def getPwd: String = {
    os.pwd.toString()
  }

  def getCsvFileList(dir:String): IndexedSeq[Path] = {
    if(dir.contains(os.root))
      os.list(Path(dir)).filter(_.ext == "csv")
    else
      os.list(pwd / dir).filter(_.ext == "csv")
  }
}
