/* My appointments: list + cancel + session receipt + per-session messaging. */
( function () {
	'use strict';
	var cfg = window.CarmillaData || {};
	var root = document.getElementById( 'cb-appointments' );
	if ( ! root ) { return; }

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

	var STATUS = { BOOKED: 'رزرو شده', CANCELLED: 'لغو شده', DONE: 'برگزار شده' };

	function load() {
		api( 'appointments' ).then( function ( items ) {
			if ( ! items || ! items.length ) {
				root.innerHTML = '<p class="t-body t-muted">هنوز نوبتی رزرو نکرده‌اید.</p>';
				return;
			}
			root.innerHTML = items.map( card ).join( '' );
			bind();
		} );
	}

	function card( a ) {
		var cancelled = a.status === 'CANCELLED';
		return '<div class="card card--pad cb-appt" data-id="' + a.id + '">' +
			'<div class="cb-appt__head"><span class="t-title-sm">' + esc( a.therapistName ) + '</span>' +
			'<span class="badge ' + ( cancelled ? '' : 'badge--new' ) + '">' + esc( STATUS[ a.status ] || a.status ) + '</span></div>' +
			'<p class="t-body-sm t-muted">' + esc( a.slot ) + '</p>' +
			'<div class="cb-appt__actions">' +
			( cancelled ? '' : '<button type="button" class="btn btn--ghost cb-appt-cancel">لغو نوبت</button>' ) +
			'<button type="button" class="btn btn--ghost cb-appt-receipt">رسید</button>' +
			'<button type="button" class="btn btn--ghost cb-appt-msg">پیام‌ها</button>' +
			'</div><div class="cb-appt__panel" hidden></div></div>';
	}

	function bind() {
		Array.prototype.forEach.call( root.querySelectorAll( '.cb-appt' ), function ( el ) {
			var id = el.getAttribute( 'data-id' );
			var panel = el.querySelector( '.cb-appt__panel' );
			var cancelBtn = el.querySelector( '.cb-appt-cancel' );
			if ( cancelBtn ) {
				cancelBtn.addEventListener( 'click', function () {
					if ( ! confirm( 'این نوبت لغو شود؟' ) ) { return; }
					post( 'appointments/' + id + '/cancel', {} ).then( load );
				} );
			}
			el.querySelector( '.cb-appt-receipt' ).addEventListener( 'click', function () {
				togglePanel( panel, function () { renderReceipt( panel, id ); } );
			} );
			el.querySelector( '.cb-appt-msg' ).addEventListener( 'click', function () {
				togglePanel( panel, function () { renderMessages( panel, id ); } );
			} );
		} );
	}

	function togglePanel( panel, render ) {
		if ( ! panel.hidden ) { panel.hidden = true; panel.innerHTML = ''; return; }
		panel.hidden = false;
		panel.innerHTML = '<p class="t-body-sm t-muted">در حال بارگذاری…</p>';
		render();
	}

	function renderReceipt( panel, id ) {
		api( 'appointments/' + id + '/receipt' ).then( function ( r ) {
			if ( ! r || r.code ) { panel.innerHTML = '<p class="t-body-sm t-muted">رسید در دسترس نیست.</p>'; return; }
			panel.innerHTML = '<div class="cb-receipt">' +
				rrow( 'مشاور', r.therapistName ) +
				( r.specialty ? rrow( 'تخصص', r.specialty ) : '' ) +
				rrow( 'زمان جلسه', r.slot ) +
				rrow( 'وضعیت', STATUS[ r.status ] || r.status ) +
				( r.priceHtml ? '<div class="cb-receipt__row"><span>هزینه</span><span>' + r.priceHtml + '</span></div>' : '' ) +
				'</div>';
		} );
	}
	function rrow( k, v ) { return '<div class="cb-receipt__row"><span>' + esc( k ) + '</span><span>' + esc( v ) + '</span></div>'; }

	function renderMessages( panel, id ) {
		panel.innerHTML = '<div class="cb-msg__list" id="cb-msg-' + id + '"></div>' +
			'<form class="cb-msg__form"><input type="text" placeholder="پیام به مشاور…" required><button class="btn btn--primary" type="submit">ارسال</button></form>';
		var list = panel.querySelector( '.cb-msg__list' );
		var form = panel.querySelector( '.cb-msg__form' );
		var input = form.querySelector( 'input' );
		function paint( msgs ) {
			if ( ! msgs || ! msgs.length ) { list.innerHTML = '<p class="t-body-sm t-muted">گفتگو را شروع کنید.</p>'; return; }
			list.innerHTML = msgs.map( function ( m ) {
				return '<div class="cb-msg__bubble ' + ( m.me ? 'cb-msg__bubble--me' : '' ) + '">' + esc( m.text ) + '</div>';
			} ).join( '' );
		}
		api( 'appointments/' + id + '/messages' ).then( paint );
		form.addEventListener( 'submit', function ( e ) {
			e.preventDefault();
			var text = input.value.trim();
			if ( ! text ) { return; }
			input.value = '';
			post( 'appointments/' + id + '/messages', { text: text } ).then( function () {
				api( 'appointments/' + id + '/messages' ).then( paint );
			} );
		} );
	}

	load();
} )();
