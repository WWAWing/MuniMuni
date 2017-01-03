# これなに
MuniMuniをJavaScriptに移植する試みです。
AltJSとしてScala.jsを採用しています。

# 環境
- IntelliJ IDEA 2016.3.1
- Scala 2.12.1
- sbt 0.13.13
- Scala.js 0.9.1

# ビルド方法

```
$ sbt
> fastOptJS
```

# 使い方
HTML内で`/target/scala-2.12/munimuni-fastopt.js`にできたソースファイルを読み込み、任意の箇所で以下のコードを挿入すると、読み込んでくれます。muni.txtを忘れずに。

```javascript
Main().main()
```
