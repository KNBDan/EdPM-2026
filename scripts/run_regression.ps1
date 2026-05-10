param(
  [Parameter(Mandatory = $true)][string]$ModelJson,
  [Parameter(Mandatory = $true)][string]$ExpectedXes,
  [string]$RscriptPath = "",
  [string]$XesName = "xesik",
  [string]$NValue = "30"
)

$args = @(
  "-Dedpm.regression.enabled=true",
  "-Dedpm.regression.model=$ModelJson",
  "-Dedpm.regression.expected.xes=$ExpectedXes",
  "-Dedpm.regression.xes.name=$XesName",
  "-Dedpm.regression.n=$NValue",
  "-Dtest=logic.regression.EdpmXesRegressionTest",
  "test"
)

if ($RscriptPath -ne "") {
  $args = @("-Dedpm.rscript=$RscriptPath") + $args
}

Write-Host "Running regression test..."
Write-Host ("mvn " + ($args -join " "))
mvn @args
