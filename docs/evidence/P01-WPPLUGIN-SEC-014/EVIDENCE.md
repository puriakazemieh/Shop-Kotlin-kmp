# Evidence for P01-WPPLUGIN-SEC-014

## Changes Made
1. **Admin Controllers**:
   - Added get_post_type() !== 'cb_course' in delete_course.
   - Added get_post_type() !== 'cb_therapist' in delete_therapist.
   - Added get_post_type() !== 'cb_psychtest' in delete_test.
   - Added get_post_type() !== 'cb_story' in delete_story.
   - Added get_post_type() !== 'shop_coupon' in delete_discount and update_discount.
   - Added get_post_type() !== 'cb_course_request' in delete_course_request.
2. **Interaction Controller**:
   - Modified owned_comment(, ) to enforce the expected comment_type.
   - Updated update_review, delete_review, update_question, delete_question to pass self::T_REVIEW or self::T_QNA.
   - This prevents Cross-Type IDOR where an attacker/admin edits/deletes a comment of a different type by knowing its ID.

## Verification
- Code syntax verified via php -l.
- All methods now enforce proper resource isolation by checking ownership boundaries and object types.
