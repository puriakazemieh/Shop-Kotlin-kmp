/* Product Q&A: load questions + post a question via theme REST. */
( function () {
	'use strict';
	var cfg = window.CarmillaData || {};
	var root = document.getElementById( 'qna' );
	if ( ! root ) { return; }
	var pid = root.getAttribute( 'data-product' );
	var list = document.getElementById( 'qna-list' );
	var formHost = document.getElementById( 'qna-form' );

	function esc( s ) {
		var d = document.createElement( 'div' );
		d.textContent = s == null ? '' : s;
		return d.innerHTML;
	}

	function answerHtml( a ) {
		return '<div class="cb-qna__answer"><span class="badge badge--new">' +
			( a.isStaff ? 'پاسخ کارشناس' : esc( a.author ) ) + '</span><p class="t-body">' +
			esc( a.content ) + '</p></div>';
	}

	function itemHtml( q ) {
		var answers = ( q.answers || [] ).map( answerHtml ).join( '' );
		return '<div class="card card--pad cb-qna__item"><div class="cb-qna__q"><span class="t-title-sm">' +
			esc( q.author ) + '</span><p class="t-body">' + esc( q.content ) + '</p></div>' +
			answers + '</div>';
	}

	function render( items ) {
		if ( ! items || ! items.length ) {
			list.innerHTML = '<p class="t-body-sm t-muted">هنوز پرسشی ثبت نشده؛ اولین نفر باشید.</p>';
			return;
		}
		list.innerHTML = items.map( itemHtml ).join( '' );
	}

	function load() {
		fetch( cfg.restUrl + 'products/' + pid + '/questions', { credentials: 'same-origin' } )
			.then( function ( r ) { return r.json(); } )
			.then( render )
			.catch( function () { list.innerHTML = ''; } );
	}

	function buildForm() {
		if ( ! cfg.loggedIn ) {
			formHost.innerHTML = '<p class="t-body-sm t-muted">برای ثبت پرسش <a href="' +
				esc( cfg.loginUrl || '#' ) + '">وارد شوید</a>.</p>';
			return;
		}
		formHost.innerHTML =
			'<form id="qna-f" class="cb-qna__form"><textarea id="qna-t" rows="2" ' +
			'placeholder="پرسش خود را درباره‌ی این محصول بنویسید…" class="cb-input"></textarea>' +
			'<button class="btn btn--primary" type="submit">ثبت پرسش</button></form>';
		document.getElementById( 'qna-f' ).addEventListener( 'submit', function ( e ) {
			e.preventDefault();
			var ta = document.getElementById( 'qna-t' );
			var text = ta.value.trim();
			if ( ! text ) { return; }
			ta.value = '';
			fetch( cfg.restUrl + 'products/' + pid + '/questions', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json', 'X-WP-Nonce': cfg.nonce },
				credentials: 'same-origin',
				body: JSON.stringify( { content: text } )
			} ).then( function ( r ) { return r.json(); } ).then( function () { load(); } );
		} );
	}

	buildForm();
	load();
} )();
