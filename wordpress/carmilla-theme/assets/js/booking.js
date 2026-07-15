/* Appointment booking: pick a slot → POST to theme REST → confirm/remove slot. */
( function () {
	'use strict';
	var cfg = window.CarmillaData || {};
	var wrap = document.getElementById( 'bk' );
	if ( ! wrap ) { return; }
	var slots = document.getElementById( 'bk-slots' );
	var result = document.getElementById( 'bk-result' );
	var tid = wrap.getAttribute( 'data-id' );

	slots.addEventListener( 'click', function ( e ) {
		var btn = e.target.closest( '.bk-slot' );
		if ( ! btn ) { return; }
		if ( ! cfg.loggedIn ) { window.location = cfg.loginUrl; return; }
		var slot = btn.getAttribute( 'data-slot' );
		btn.disabled = true;

		fetch( cfg.restUrl + 'appointments', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json', 'X-WP-Nonce': cfg.nonce },
			credentials: 'same-origin',
			body: JSON.stringify( { therapistId: tid, slot: slot } )
		} ).then( function ( r ) { return r.json().then( function ( d ) { return { ok: r.ok, d: d }; } ); } )
		.then( function ( res ) {
			if ( res.ok && res.d && res.d.id ) {
				btn.remove();
				result.innerHTML = '<div class="card card--pad"><span class="badge badge--stock">ثبت شد</span> <span class="t-body" style="margin:0">نوبت شما رزرو شد. وضعیت را در «حساب کاربری» می‌بینید.</span></div>';
			} else {
				btn.disabled = false;
				result.innerHTML = '<p class="t-body" style="color:var(--sale);margin:0">' + ( ( res.d && res.d.message ) || 'خطا در رزرو.' ) + '</p>';
			}
			result.scrollIntoView( { behavior: 'smooth' } );
		} );
	} );
} )();
