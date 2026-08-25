/* Academy interactive widgets:
   #cb-quiz (CourseQuizScreen), #cb-project (ProjectSubmission+PeerReview),
   #cb-placement (PlacementQuizScreen), #cb-verify (CertificateVerifyScreen). */
( function () {
	'use strict';
	var cfg = window.CarmillaData || {};

	function esc( s ) { var d = document.createElement( 'div' ); d.textContent = s == null ? '' : s; return d.innerHTML; }
	function api( path, opts ) {
		opts = opts || {};
		opts.credentials = 'same-origin';
		opts.headers = Object.assign( { 'X-WP-Nonce': cfg.nonce }, opts.headers || {} );
		return fetch( cfg.restUrl + path, opts ).then( function ( r ) { return r.json(); } );
	}
	function post( path, body ) {
		return api( path, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify( body || {} ) } );
	}

	/* ---------- Course final quiz ---------- */
	var quizRoot = document.getElementById( 'cb-quiz' );
	if ( quizRoot ) {
		var cid = quizRoot.getAttribute( 'data-course' );
		var answers = {};
		api( 'courses/' + cid + '/quiz' ).then( function ( q ) {
			if ( ! q || ! q.hasQuiz ) {
				quizRoot.innerHTML = '<p class="t-body-sm t-muted">برای این دوره آزمونی تعریف نشده است.</p>';
				return;
			}
			renderQuiz( q );
		} );

		function renderQuiz( q ) {
			var html = '<p class="t-body-sm t-muted">برای دریافتِ گواهی باید حداقل ' + esc( q.passScore ) + '٪ سؤالات را درست پاسخ دهید.</p>';
			html += q.questions.map( function ( item, n ) {
				var opts = item.options.map( function ( o, i ) {
					return '<label class="cb-quiz__opt"><input type="radio" name="q' + item.index + '" value="' + i + '"><span>' + esc( o ) + '</span></label>';
				} ).join( '' );
				return '<div class="card card--pad cb-quiz__q"><p class="cb-quiz__text">' + esc( ( n + 1 ) + '. ' + item.text ) + '</p>' + opts + '</div>';
			} ).join( '' );
			html += '<button type="button" class="btn btn--primary cb-quiz__submit" disabled>ثبتِ پاسخ‌ها</button>';
			quizRoot.innerHTML = html;

			var submit = quizRoot.querySelector( '.cb-quiz__submit' );
			Array.prototype.forEach.call( quizRoot.querySelectorAll( 'input[type=radio]' ), function ( r ) {
				r.addEventListener( 'change', function () {
					answers[ r.name.substring( 1 ) ] = parseInt( r.value, 10 );
					submit.disabled = Object.keys( answers ).length < q.questions.length;
				} );
			} );
			submit.addEventListener( 'click', function () {
				submit.disabled = true; submit.textContent = 'در حالِ ارسال…';
				post( 'courses/' + cid + '/quiz/submit', { answers: answers } ).then( renderResult );
			} );
		}

		function renderResult( r ) {
			if ( ! r || r.code ) { quizRoot.innerHTML = '<p class="t-body-sm t-muted">خطا در ثبت آزمون.</p>'; return; }
			var msg = r.passed ? 'تبریک! قبول شدید' : 'متأسفانه قبول نشدید';
			var html = '<div class="cb-quiz__result ' + ( r.passed ? 'is-pass' : 'is-fail' ) + '">' +
				'<div class="cb-quiz__badge">' + ( r.passed ? '✓' : '✕' ) + '</div>' +
				'<h3 class="t-title-sm">' + esc( msg ) + '</h3>' +
				'<p class="t-body-sm">نمره‌ی شما: ' + esc( r.score ) + '٪ (حدِ نصاب: ' + esc( r.passScore ) + '٪)</p>';
			if ( r.passed && r.certNumber ) {
				html += '<p class="t-body-sm">شماره‌ی گواهیِ صادرشده: <strong>' + esc( r.certNumber ) + '</strong></p>';
			}
			html += '<button type="button" class="btn btn--ghost cb-quiz__retry">تلاشِ دوباره</button></div>';
			quizRoot.innerHTML = html;
			quizRoot.querySelector( '.cb-quiz__retry' ).addEventListener( 'click', function () {
				answers = {};
				api( 'courses/' + cid + '/quiz' ).then( renderQuiz );
			} );
		}
	}

	/* ---------- Project submission + peer review ---------- */
	var projRoot = document.getElementById( 'cb-project' );
	if ( projRoot ) {
		var pcid = projRoot.getAttribute( 'data-course' );
		renderProject();

		function statusView( s ) {
			if ( s.status === 'APPROVED' ) { return { label: 'تأییدشده ✓', cls: 'is-ok' }; }
			if ( s.status === 'REJECTED' ) { return { label: 'ردشده — لطفاً اصلاح و دوباره ثبت کنید', cls: 'is-bad' }; }
			return { label: 'در انتظارِ بررسی', cls: '' };
		}

		function renderProject() {
			api( 'courses/' + pcid + '/project' ).then( function ( r ) {
				var s = r && r.submission;
				var html = '';
				if ( s ) {
					var sv = statusView( s );
					html += '<div class="card card--pad cb-proj__status ' + sv.cls + '">' +
						'<p class="t-body-sm"><strong>وضعیتِ پروژه:</strong> ' + esc( sv.label ) + '</p>' +
						'<p class="t-body-sm t-muted">لینک: ' + esc( s.fileUrl ) + '</p>' +
						( s.mentorFeedback ? '<p class="t-body-sm">بازخوردِ مدرس: ' + esc( s.mentorFeedback ) + '</p>' : '' ) +
						'</div>';
				}
				html += '<div class="cb-proj__form">' +
					'<input type="url" class="cb-proj__url" placeholder="لینکِ پروژه (گیت‌هاب/درایو/...)">' +
					'<input type="text" class="cb-proj__note" placeholder="توضیحِ تکمیلی (اختیاری)">' +
					'<button type="button" class="btn btn--primary cb-proj__submit">' + ( s ? 'ثبتِ دوباره' : 'ثبتِ پروژه' ) + '</button>' +
					'</div>' +
					'<h3 class="t-title-sm" style="margin-block-start:var(--sp-lg)">نقدِ همتایان</h3>' +
					'<div class="cb-peer"></div>';
				projRoot.innerHTML = html;

				projRoot.querySelector( '.cb-proj__submit' ).addEventListener( 'click', function () {
					var url = projRoot.querySelector( '.cb-proj__url' ).value.trim();
					if ( ! url ) { return; }
					post( 'courses/' + pcid + '/project', {
						fileUrl: url,
						note: projRoot.querySelector( '.cb-proj__note' ).value.trim()
					} ).then( renderProject );
				} );
				renderPeers();
			} );
		}

		function renderPeers() {
			var wrap = projRoot.querySelector( '.cb-peer' );
			api( 'courses/' + pcid + '/peer' ).then( function ( subs ) {
				if ( ! subs || ! subs.length ) { wrap.innerHTML = '<p class="t-body-sm t-muted">هنوز پروژه‌ی تأییدشده‌ای برای نقد نیست.</p>'; return; }
				wrap.innerHTML = subs.map( function ( s ) {
					return '<div class="card card--pad cb-peer__item" data-sid="' + s.id + '">' +
						'<strong>' + esc( s.userName ) + '</strong>' +
						'<p class="t-body-sm t-muted">لینک: ' + esc( s.fileUrl ) + '</p>' +
						( s.note ? '<p class="t-body-sm">' + esc( s.note ) + '</p>' : '' ) +
						'<div class="cb-peer__comments" hidden></div>' +
						'<button type="button" class="cb-peer__toggle">نمایش/افزودن نظر</button>' +
						'</div>';
				} ).join( '' );
				Array.prototype.forEach.call( wrap.querySelectorAll( '.cb-peer__item' ), function ( el ) {
					var sid = el.getAttribute( 'data-sid' );
					var box = el.querySelector( '.cb-peer__comments' );
					el.querySelector( '.cb-peer__toggle' ).addEventListener( 'click', function () {
						if ( ! box.hidden ) { box.hidden = true; return; }
						box.hidden = false;
						box.innerHTML = '<p class="t-body-sm t-muted">در حالِ بارگذاری…</p>';
						loadComments( sid, box );
					} );
				} );
			} );
		}

		function loadComments( sid, box ) {
			api( 'submissions/' + sid + '/comments' ).then( function ( comments ) {
				var list = ( comments && comments.length )
					? comments.map( function ( c ) { return '<div class="cb-peer__c"><strong>' + esc( c.userName ) + '</strong> ' + esc( c.comment ) + '</div>'; } ).join( '' )
					: '<p class="t-body-sm t-muted">هنوز نظری ثبت نشده.</p>';
				box.innerHTML = list + '<div class="cb-peer__form"><input type="text" placeholder="نظرت را بنویس…"><button type="button" class="btn btn--ghost">ارسال</button></div>';
				var input = box.querySelector( 'input' );
				box.querySelector( 'button' ).addEventListener( 'click', function () {
					var t = input.value.trim();
					if ( ! t ) { return; }
					input.value = '';
					post( 'submissions/' + sid + '/comments', { comment: t } ).then( function () { loadComments( sid, box ); } );
				} );
			} );
		}
	}

	/* ---------- Placement quiz ---------- */
	var placeRoot = document.getElementById( 'cb-placement' );
	if ( placeRoot ) {
		var questions = [];
		try { questions = JSON.parse( placeRoot.getAttribute( 'data-questions' ) || '[]' ); } catch ( e ) {}
		var pAnswers = {};
		function renderPlacement() {
			var html = questions.map( function ( q, qi ) {
				var opts = q.options.map( function ( o, oi ) {
					return '<label class="cb-quiz__opt"><input type="radio" name="p' + qi + '" value="' + oi + '"><span>' + esc( o ) + '</span></label>';
				} ).join( '' );
				return '<div class="card card--pad cb-quiz__q"><p class="cb-quiz__text">' + esc( q.text ) + '</p>' + opts + '</div>';
			} ).join( '' );
			html += '<button type="button" class="btn btn--primary cb-place__submit" disabled>مشاهده‌ی نتیجه</button>';
			placeRoot.innerHTML = html;
			var submit = placeRoot.querySelector( '.cb-place__submit' );
			Array.prototype.forEach.call( placeRoot.querySelectorAll( 'input[type=radio]' ), function ( r ) {
				r.addEventListener( 'change', function () {
					pAnswers[ r.name.substring( 1 ) ] = parseInt( r.value, 10 );
					submit.disabled = Object.keys( pAnswers ).length < questions.length;
				} );
			} );
			submit.addEventListener( 'click', function () {
				submit.disabled = true; submit.textContent = 'در حالِ محاسبه…';
				post( 'placement/submit', { answers: questions.map( function ( _, i ) { return pAnswers[ i ] || 0; } ) } ).then( function ( r ) {
					placeRoot.innerHTML = '<div class="cb-place__result"><p class="t-body-sm t-muted">سطحِ پیشنهادیِ شما:</p>' +
						'<h2 class="t-headline cb-place__level">' + esc( r.label ) + '</h2>' +
						'<a class="btn btn--primary" href="' + esc( r.url ) + '">مشاهده‌ی دوره‌های همین سطح</a></div>';
				} );
			} );
		}
		if ( questions.length ) { renderPlacement(); }
	}

	/* ---------- Certificate verification ---------- */
	var verifyRoot = document.getElementById( 'cb-verify' );
	if ( verifyRoot ) {
		var input = verifyRoot.querySelector( '.cb-verify__input' );
		var out = verifyRoot.querySelector( '.cb-verify__result' );
		verifyRoot.querySelector( '.cb-verify__btn' ).addEventListener( 'click', function () {
			var code = input.value.trim();
			if ( ! code ) { return; }
			out.innerHTML = '<p class="t-body-sm t-muted">در حالِ بررسی…</p>';
			api( 'certificates/verify?code=' + encodeURIComponent( code ) ).then( function ( r ) {
				if ( r && r.valid ) {
					out.innerHTML = '<div class="cb-verify__ok"><strong>گواهیِ معتبر ✓</strong>' +
						'<p class="t-body-sm">دوره: ' + esc( r.courseTitle ) + '</p>' +
						( r.issuedAt ? '<p class="t-body-sm t-muted">تاریخِ صدور: ' + esc( ( r.issuedAt + '' ).substring( 0, 10 ) ) + '</p>' : '' ) +
						'</div>';
				} else {
					out.innerHTML = '<div class="cb-verify__bad"><strong>گواهی‌ای با این شماره یافت نشد.</strong></div>';
				}
			} );
		} );
	}
} )();
