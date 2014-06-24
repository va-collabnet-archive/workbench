#! /bin/sh 
# revision 2010-02-08

#copies relevant files to a second directory for publishing.

CURRENT=`pwd`
OUT=`basename $CURRENT`.pub.`date "+%Y-%m-%d"`
TARGET=../publish//`basename $CURRENT`.pub.`date "+%Y-%m-%d"`
SERVER=mail.montagesystems.com.au
PDF_DIR=out-XEP_OT1.5
#XHTML_DIR=out-XHTML_OT1.5
XHTML_DIR=out-oXygen-XHTML


echo building HTML pages in $TARGET

#first ensure we have logo available for autogenerated files.

SPECIALDIRS="./Business_Process_Library ./Task_Library"
for i in $SPECIALDIRS
do
IFS=$'\n'

    for d in `find  $i -type d | sed -e 's@/[^/]*$@@' | sort | uniq | grep -v '^.$'`
    do
    #echo "-- $d"
       IMAGEDIR=$d/images
       mkdir $IMAGEDIR 2>/dev/null
       #echo "cp -pr images/IHTSDO_logohead_full_300.png $IMAGEDIR"
       cp -pr images/IHTSDO_logohead_full_300.png $IMAGEDIR
    done
unset IFS
done


if (test -d $TARGET)
 then 
   echo "please remove $TARGET and rerun"
   exit
fi

mkdir $TARGET

#Tidy up table of contents file...

sed -e 's@</head>@</script--> \
        <STYLE TYPE="text/css"> \
                .color { background-color: #ddeeff; }   \
             H1 {font-family: Arial; font-size: 14pt; Color:red} \
             X_UL  {list-style-type: square ; margin-left:-20px; margin-right:0px;} \
             X_LI>UL  {list-style-type: disc } \
             A  { font-family: Arial; font-size:9pt; line-height:12pt} \
             A     {text-decoration:none; } \
             A:link                {text-decoration:none; color: black} \
             A.chapter { font-weight: bold } \
             A:visited { color: #666666 }   \
             A:hover   { color: #99ccaa } \
             A:active  { color: orange } \
             H3.logo   { font-family: Arial; margin: 0pt 0pt; color: darkred } \
              \
        </STYLE> \
</head> @' \
    -e 's@<body>@<body class="color"> \
<table align="center" bgcolor="white" border="0"> \
 <tr> \
   <td><img src="images/IHTSDO_logo.png" width=60px height=50px alt="IHTSDO"></td> \
   <td><table> \
         <tr><td><h3 class="logo" align="center">IHTSDO Workbench</h2></td></tr> \
         <tr><td><h3 class="logo" align="center">CONTENTS</h3></td></tr> \
       </table></td> \
 </tr> \
</table> \
@' \
    -e 's@href="../@href="@' \
< $XHTML_DIR/index.html > toc.html
cp $XHTML_DIR/frontCover.html .

echo copying files to $TARGET
find . -iname "*.html" -o -iname "*.css" -o -iname "*.jpg" -o -iname "*.png" | grep -v '/out-' |grep -v '/out_' |grep -v '/temp/' | cpio -pmud $TARGET
find $XHTML_DIR -iname "*.css" | cpio -pmud $TARGET
echo "files copied to $TARGET"
echo "jarring the files..."
(cd ../publish && jar cf $OUT.jar $OUT)
echo "copying jar file $TARGET to healthbase server.."
scp ${TARGET}.jar eric@${SERVER}:/srv/www/healthbase/cip/html/ii/
#echo "copying pdf to healthbase server.."
#scp $PDF_DIR/Guidebook/Workbench_Guidebook.pdf eric@${SERVER}:/srv/www/healthbase/cip/html/ii/
echo "done"
