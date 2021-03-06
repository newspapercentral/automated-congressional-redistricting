FILES=../data/tab*
for f in $FILES
do
  echo "Processing $f file..."
  FIPS=${f:21:2}
  echo $FIPS  
  case $FIPS in
  01)
    STATE="al"
    K="7"
    ;;
  05)
    STATE="ar"
    K="4"
    ;;
  04)
    STATE="az"
    K="9"
    ;;
  06)
    STATE="ca"
    K="53"
    ;;
  08)
    STATE="co"
    K="7"
    ;;
  09)
    STATE="ct"
    K="5"
    ;;
  12)
    STATE="fl"
    K="27"
    ;;
  13)
    STATE="ga"
    K="14"
    ;;
  15)
    STATE="hi"
    K="2"
    ;;
  19)
    STATE="ia"
    K="4"
    ;;
  16)
    STATE="id"
    K="2"
    ;;
  17)
    STATE="il"
    K="18"
    ;;
  18)
    STATE="in"
    K="9"
    ;;
  20)
    STATE="ks"
    K="4"
    ;;
  21)
    STATE="ky"
    K="6"
    ;;
  22)
    STATE="la"
    K="6"
    ;;
  25)
    STATE="ma"
    K="9"
    ;;
  24)
    STATE="md"
    K="8"
    ;;
  23)
    STATE="me"
    K="2"
    ;;
  26)
    STATE="mi"
    K="14"
    ;;
  27)
    STATE="mn"
    K="8"
    ;;
  29)
    STATE="mo"
    K="8"
    ;;
  28)
    STATE="ms"
    K="4"
    ;;
  37)
    STATE="nc"
    K="13"
    ;;
  31)
    STATE="ne"
    K="3"
    ;;
  33)
    STATE="nh"
    K="2"
    ;;
  34)
    STATE="nj"
    K="12"
    ;;
  35)
    STATE="nm"
    K="3"
    ;;
  32)
    STATE="nv"
    K="4"
    ;;
  36)
    STATE="ny"
    K="27"
    ;;
  39)
    STATE="oh"
    K="16"
    ;;
  40)
    STATE="ok"
    K="5"
    ;;
  41)
    STATE="or"
    K="5"
    ;;
  42)
    STATE="pa"
    K="18"
    ;;
  44)
    STATE="ri"
    K="2"
    ;;
  45)
    STATE="sc"
    K="7"
    ;;
  47)
    STATE="tn"
    K="9"
    ;;
  48)
    STATE="tx"
    K="36"
    ;;
  49)
    STATE="ut"
    K="4"
    ;;
  51)
    STATE="va"
    K="11"
    ;;
  53)
    STATE="wa"
    K="10"
    ;;
  55)
    STATE="wi"
    K="8"
    ;;
  54)
    STATE="wv"
    K="3"
    ;;
  *)
    STATE="none"
    K="1"
    FIPS="0"
    ;;
  esac
  echo $STATE
  SITE='point'
  MAX_FUNC='pop'
  UNIT='block'
  SWAP='true'
  LOGFILE='../logs/dac'"$FIPS$UNIT$SWAP$MAX_FUNC$SITE"'.txt'
if [ "$STATE" == "ny" ] || [ "$STATE" == "fl" ]; then
  (time java -jar -Xmx80000m '../jars/dac.jar' "$STATE" "$K" '/home/data' 'true' "$UNIT" "$SWAP" 'true' "$MAX_FUNC" "$SITE") &> $LOGFILE 
else
    echo "skipping..."
fi
done
