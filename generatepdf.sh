### Create a single PDF file for the documentation ###
######################################################


## Settings ##
declare -a HTMLFILES=(
"readme.html"
"target/site/project-info.html"
"target/site/project-summary.html"
"target/site/license.html"
"target/site/source-repository.html"
"target/site/issue-tracking.html"
"target/site/dependencies.html"
"target/site/dependency-info.html"
"target/site/failsafe-report.html"
"target/site/surefire-report.html"
"target/site/plugin-management.html"
"target/site/plugins.html"
"target/site/project-reports.html"
"target/site/apidocs/com/caran/agaadapter/package-summary.html"
"target/site/apidocs/com/caran/agaadapter/package-tree.html"
"target/site/apidocs/com/caran/agaadapter/AGAAdapter.html"
"target/site/apidocs/com/caran/agaadapter/ConversionInfo.html"
"target/site/apidocs/com/caran/agaadapter/SignalInfo.html"
"target/site/apidocs/com/caran/agaadapter/LoadConversionConfig.html"
"target/site/apidocs/com/caran/agaadapter/LoadProperties.html"
"target/site/apidocs/index-all.html"
"target/site/apidocs/help-doc.html"
"target/site/cobertura/frame-summary-com.caran.agaadapter.html"
"target/site/cobertura/com.caran.agaadapter.AGAAdapter.html"
"target/site/cobertura/com.caran.agaadapter.ConversionInfo.html"
"target/site/cobertura/com.caran.agaadapter.SignalInfo.html"
"target/site/cobertura/com.caran.agaadapter.LoadConversionConfig.html"
"target/site/cobertura/com.caran.agaadapter.LoadProperties.html"
"target/site/pmd.html"
"target/site/checkstyle.html"
)


## Verify installed programs ##
# Based on http://stackoverflow.com/questions/592620/check-if-a-program-exists-from-a-bash-script
command -v mvn >/dev/null 2>&1 || \
    { echo >&2 "The maven tool is not installed. Can not create PDF documentation."; exit 1; }
command -v wkhtmltopdf >/dev/null 2>&1 || \
    { echo >&2 "The wkhtmltopdf tool is not installed. Can not create PDF documentation."; exit 1; }
command -v pdfunite >/dev/null 2>&1 || \
    { echo >&2 "The pdfunite tool is not installed. Can not create PDF documentation."; exit 1; }
command -v markdown >/dev/null 2>&1 || \
    { echo >&2 "The markdown tool is not installed. Can not create PDF documentation."; exit 1; }


## Create HTML pages ##
mvn site
markdown README.md > readme.html


## Create individual PDF pages ##
let i=1
for htmlfile in "${HTMLFILES[@]}"
do
    echo " "
    echo "Converting $htmlfile (${i})"
    wkhtmltopdf $htmlfile ${i}_temp.pdf
    let i++
done


## Join the individual PDF pages ##
temporary_pdf_files=`ls -v *_temp.pdf`
pdfunite $temporary_pdf_files target/site/AGAAdapterDocumentation.pdf
rm *_temp.pdf
