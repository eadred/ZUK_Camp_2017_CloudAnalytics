REM Change these as appropriate
SET dataset=dataflow_demo
SET table=popular_hashtags

bq mk %dataset%
bq mk --schema=popular_hashtags.json -t %dataset%.%table%