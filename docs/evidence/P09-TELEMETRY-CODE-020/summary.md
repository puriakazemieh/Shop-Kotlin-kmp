# P09-TELEMETRY-CODE-020 Evidence

- Runtime: `POST /wp-json/carmilla/v1/telemetry/events` در WordPress QA با event synthetic `pwa_installed` پاسخ `202 {"accepted":true}` داد.
- Manual QA: 2026-10-13،qa_admin،PASS. Dashboard `Carmilla → Telemetry` bucket `2026-10-13|local-default|pwa|pwa_installed` با count `1` را نمایش داد.
- Privacy: تنها aggregate ذخیره شد؛ payload هویتی،پرداخت یا سلامت وارد Evidence نشده است.
