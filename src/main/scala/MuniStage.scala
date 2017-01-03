import org.scalajs.dom.ext.Ajax

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js.annotation.JSExport

case class MuniStage( stage: String,
                      level: Int,
                      chips: List[Char],
                      map: List[List[Char]]
                    )

object MuniStages {
  @JSExport
  def load(): Future[Either[String, List[MuniStage]]] = {
    loadFile().map(f => createMuniStages(f))
  }

  private def createMuniStages(file: String): Either[String, List[MuniStage]] = {
    // ファイルをステージごとに分割
    val splittedFile = splitStageData(file)
    // ステージファイルの正当性をチェック
    val checkedStage = splittedFile.map{ stage =>
      for (
        name <- stage.find(_.head == "$STAGE") match {
          case Some(s) => checkStageName(s.last)
          case None => Left("Stage name not found.")
        };
        level <- stage.find(_.head == "$LEVEL") match {
          case Some(s) => checkLevel(s.last)
          case None => Left("Level not found.")
        };
        chips <- stage.find(_.head == "$CHIPS") match {
          case Some(s) => checkChips(s.last)
          case None => Left("Chips not found.")
        };
        map <- stage.find(_.head == "$MAP") match {
          case Some(s) => checkMap(s.tail)
          case None => Left("Map not found.")
        }
      ) yield MuniStage(name, level, chips, map)
    }
    // モナドの中外を入れ替える
    // List[Either[String, MuniStage]] => Either[String, List[MuniStage]]
    checkedStage.foldRight (Right(Nil): Either[String, List[MuniStage]]) { (muniStage, acc) =>
      for (
        a <- acc;
        m <- muniStage
      ) yield m :: a
    }
  }

  private def loadFile(): Future[String] = {
    val file = Ajax.get("./muni.txt")
    file.map(_.responseText)
  }

  /**
    * 読み込んだステージファイルをステージごとに分離
    * @param file ステージファイル
    * @return 分離したステージファイル
    */
  private def splitStageData(file: String): List[List[List[String]]] = {
    // $STAGEで切って付け直す
    // リストの先頭に空文字列ができてしまうためtailで取り除く
    val stages: List[String] = file.split("\\$STAGE").toList.tail.map("$STAGE" + _)

    @tailrec
    def splitStage(stage: List[String], result: List[List[String]]): List[List[String]] = {
      stage.headOption match {
        case Some(s) => s match {
          case "$STAGE" | "$LEVEL" | "$CHIPS" | "$SPAN" => splitStage(stage.drop(2), stage.take(2) :: result)
          case "$MAP" => splitStage(stage.drop(15), stage.take(15) :: result)
          case _ => result
        }
        case None => result
      }
    }

    // ステージごとにそれぞれの$部分を切り離す
    stages.map(s => splitStage(s.split('\n').toList, List()))
  }

  /**
    * ステージ名を(事実上)チェック
    * 今のところただの型合わせ用
    * TODO: XSS防止の仕組み
    * @param name チェックしたいステージ名
    * @return チェック済みのステージ名
    */
  private def checkStageName(name: String): Either[String, String] = {
    Right(name)
  }

  /**
    * レベルをチェック
    * 数値かどうか、1 ~ 5以外の値が使われていないかを確認
    * @param level チェックしたいレベルの値
    * @return チェック済みの値
    */
  private def checkLevel(level: String): Either[String, Int] = {
    try {
      level.toInt match {
        case l if 1 <= l && l <= 5 => Right(l)
        case _ => Left("Illegal level.")
      }
    } catch {
      case _: NumberFormatException => Left("Illegal level.")
    }
  }

  /**
    * むにむに列のチェック
    * 1 ~ 3以外の値が使われていないか、数が100個以内かを確認
    * @param chips チェックしたいむにむに列
    * @return チェック済みの値
    */
  private def checkChips(chips: String): Either[String, List[Char]] = {
    if (chips.length > 100) {
      Left("Chips over.")
    } else if (!chips.filter(c => c !='1' && c != '2' && c != '3').isEmpty) {
      Left("Illegal string.")
    } else {
      Right(chips.toList)
    }
  }

  /**
    * マップのチェック
    * #, ., F以外の値が使われていないか、サイズが18x14に収まっているかを確認
    * @param map チェックしたいマップ
    * @return チェック済みの値
    */
  private def checkMap(map: List[String]): Either[String, List[List[Char]]] = {
    if (map.length < 14 || map.map(_.length).exists(_ < 18)) {
      Left("Map line under.")
    } else if (map.map(_.length).exists(_ > 18)) {
      Left("Map line over.")
    } else if (map.map(_.filter(c => c != '#' && c != '.' && c != 'F').isEmpty).forall(!_)) {
      Left("Illegal string.")
    } else {
      Right(map.map(_.toList))
    }
  }
}