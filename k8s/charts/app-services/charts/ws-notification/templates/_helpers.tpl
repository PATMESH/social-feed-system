{{- define "fullname" -}}
{{- printf "%s" .Chart.Name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- define "name" -}}
{{- .Chart.Name -}}
{{- end -}}
