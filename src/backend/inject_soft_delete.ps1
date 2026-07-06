$files = Get-ChildItem -Path "d:\Registru-Agricol\Registru-Agricol\src\backend\src\main\java" -Recurse -Filter *.java | Where-Object {
    (Select-String -Path $_.FullName -Pattern "@Entity" -Quiet) -and 
    ($_.Name -ne "CustomRevisionEntity.java") -and 
    ($_.Name -ne "GdprAuditLog.java")
}

foreach ($file in $files) {
    $content = Get-Content $file.FullName
    
    # Check if already processed
    if ($content -match "deleted = false") {
        Write-Host "Skipping $($file.Name) - already processed"
        continue
    }

    # Find table name
    $tableName = ""
    $tableLine = $content | Select-String -Pattern '@Table\(.*name\s*=\s*"([^"]+)"'
    if ($tableLine) {
        $tableName = $tableLine.Matches[0].Groups[1].Value
    } else {
        # Fallback to class name if no @Table name
        $classLine = $content | Select-String -Pattern 'public class (\w+)'
        if ($classLine) {
            $tableName = $classLine.Matches[0].Groups[1].Value
        }
    }

    if ($tableName -eq "") {
        Write-Host "Could not find table name for $($file.Name)"
        continue
    }

    $newContent = @()
    $hasSqlDeleteImport = $false
    $hasSqlRestrictionImport = $false
    $inClass = $false
    $classFound = $false

    foreach ($line in $content) {
        if ($line -match "import org.hibernate.annotations.SQLDelete") { $hasSqlDeleteImport = $true }
        if ($line -match "import org.hibernate.annotations.SQLRestriction") { $hasSqlRestrictionImport = $true }

        if (-not $classFound -and $line -match "^(.*)public class (\w+)") {
            $classFound = $true
            # Insert annotations before class
            $newContent += "@SQLDelete(sql = `"UPDATE $tableName SET deleted = true WHERE id=?`")"
            $newContent += "@SQLRestriction(`"deleted = false`")"
            $newContent += $line
            
            # Insert field after class definition
            if ($line -match "\{") {
                $newContent += "    @jakarta.persistence.Column(nullable = false)"
                $newContent += "    private boolean deleted = false;"
                $newContent += ""
            } else {
                # The brace might be on the next line
                $inClass = $true
            }
        } elseif ($inClass -and $line -match "\{") {
            $inClass = $false
            $newContent += $line
            $newContent += "    @jakarta.persistence.Column(nullable = false)"
            $newContent += "    private boolean deleted = false;"
            $newContent += ""
        } else {
            $newContent += $line
            
            # Inject imports right after package declaration
            if ($line -match "^package ") {
                $newContent += ""
                if (-not $hasSqlDeleteImport) {
                    $newContent += "import org.hibernate.annotations.SQLDelete;"
                    $hasSqlDeleteImport = $true
                }
                if (-not $hasSqlRestrictionImport) {
                    $newContent += "import org.hibernate.annotations.SQLRestriction;"
                    $hasSqlRestrictionImport = $true
                }
            }
        }
    }
    
    $newContent | Set-Content $file.FullName -Encoding UTF8
    Write-Host "Processed $($file.Name) with table $tableName"
}
