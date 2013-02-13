import Text.XML.HXT.Core
import Data.List
import System.Environment
import System.Exit

diffs up my = do 
  upstream <- runX (processor up) 
  mine <-  runX (processor my)           
  putStrLn "Missing:"
  print (upstream \\ mine)
  putStrLn "\nDeleted:"
  print (mine \\ upstream)



processor :: FilePath -> IOSArrow XmlTree String
processor filename =
    readDocument [withValidate no] filename >>>
    multi (isElem >>> hasName "string") >>>
    getAttrl >>> isAttr >>> hasName "name" >>>
    getChildren >>> getText


 
main :: IO ()
main = do
  args <- getArgs
  p <- getProgName
  case length args of
    2 -> diffs (head args) (last args)
    _ -> do 
      putStrLn $ "Usage: " ++ p ++ " res/values/strings.xml res/values-it/strings.xml"
      exitFailure


