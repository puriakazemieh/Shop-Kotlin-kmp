import re

path = 'core/network/src/commonMain/kotlin/com/kazemieh/network/common/HttpClientFactory.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace encodedPath with just .build().encodedPath or similar. Actually, let's just use request.url.encodedPath but we need to import io.ktor.http.encodedPath.
# Or better yet, we can use request.url.build().encodedPath
# Wait, let's just check request.url.build().toString()

content = content.replace('request.url.encodedPath', 'request.url.build().encodedPath')

# Replace the ApiException throw with standard Exception
old_throw = """throw ApiException(
                messageText = Res.string.unknownError, // Using unknownError as fallback string resource
                code = 403
            )"""
new_throw = 'throw Exception("Feature " + featureId + " is disabled by FeatureGuardPlugin")'
content = content.replace(old_throw, new_throw)
content = content.replace('import com.kazemieh.network.common.ApiException\nimport com.kazemieh.common.Res', '')

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
