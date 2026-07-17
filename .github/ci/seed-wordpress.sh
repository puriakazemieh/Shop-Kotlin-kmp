#!/usr/bin/env bash
# Seed a fresh WordPress install with demo content so the Carmilla theme has
# something to render, then emit concrete screenshot routes to shots/wp-routes.json.
set -euo pipefail

WP="wp --allow-root --path=$WP_PATH"
BASE="${WP_BASE:-http://localhost:8090}"

echo "== plugins/theme =="
$WP plugin install woocommerce --activate || echo "woo install failed (continuing)"
$WP plugin activate carmilla-bridge || echo "bridge activate failed (continuing)"
$WP theme activate carmilla-theme
$WP rewrite structure '' --hard || true   # plain permalinks (built-in server, no rewrite)

echo "== demo products (WooCommerce) =="
PROD_IDS=()
add_product () {
  local id
  id=$($WP wc product create --name="$1" --type=simple --regular_price="$2" \
        --sale_price="$3" --description="$4" --status=publish --user=1 --porcelain 2>/dev/null || echo "")
  if [ -n "$id" ]; then PROD_IDS+=("$id"); fi
}
add_product "ساعت مچی کلاسیک کارمیلا" "4800000" "3990000" "ساعت مچیِ اتوماتیک با بندِ چرمِ طبیعی." || true
add_product "عطر زنانه رز نویر" "2100000" "" "رایحه‌ی گرمِ گلِ رز و مشک." || true
add_product "کیف چرم دست‌دوز" "1650000" "1290000" "کیفِ چرمِ گاویِ دست‌دوز." || true
add_product "هدفون بی‌سیم پرو" "3200000" "" "نویزکنسلینگِ فعال با شارژِ ۳۰ ساعته." || true

echo "== demo blog posts =="
$WP post create --post_type=post --post_status=publish --post_title="راهنمای انتخاب ساعت مچی" --post_content="متنِ نمونه‌ی مقاله برای نمایشِ قالب." --user=1 || true
$WP post create --post_type=post --post_status=publish --post_title="۵ نکته درباره عطر" --post_content="متنِ نمونه‌ی دوم." --user=1 || true

echo "== vertical CPTs (registered by carmilla-bridge) =="
COURSE_ID=$($WP post create --post_type=cb_course --post_status=publish --post_title="دوره‌ی جامع عکاسی" --post_content="از صفر تا پیشرفته." --user=1 --porcelain || echo "")
$WP post create --post_type=cb_therapist --post_status=publish --post_title="دکتر مریم احمدی" --post_content="روان‌شناس بالینی." --user=1 || true
TEST_ID=$($WP post create --post_type=cb_psychtest --post_status=publish --post_title="تست شخصیت‌شناسی MBTI" --post_content="۱۶ تیپِ شخصیتی." --user=1 --porcelain || echo "")

echo "== front page =="
FP=$($WP post create --post_type=page --post_status=publish --post_title="خانه" --user=1 --porcelain || echo "")
if [ -n "$FP" ]; then
  $WP option update show_on_front page || true
  $WP option update page_on_front "$FP" || true
fi

echo "== routes.json =="
mkdir -p shots
{
  echo '['
  echo '  {"name":"home","url":"/"},'
  echo '  {"name":"shop","url":"/?post_type=product"},'
  if [ "${#PROD_IDS[@]}" -gt 0 ]; then
    echo "  {\"name\":\"product\",\"url\":\"/?post_type=product&p=${PROD_IDS[0]}\"},"
  fi
  echo '  {"name":"blog","url":"/?post_type=post"},'
  echo '  {"name":"courses","url":"/?post_type=cb_course"},'
  if [ -n "$COURSE_ID" ]; then echo "  {\"name\":\"course\",\"url\":\"/?post_type=cb_course&p=$COURSE_ID\"},"; fi
  echo '  {"name":"therapists","url":"/?post_type=cb_therapist"},'
  echo '  {"name":"psychtests","url":"/?post_type=cb_psychtest"}'
  echo ']'
} > shots/wp-routes.json
cat shots/wp-routes.json
echo "== seed done =="
