/* Clinic file tabs: mood check-in, journal, homework (my-account). */
( function () {
	'use strict';
	var cfg = window.CarmillaData || {};
	var root = document.getElementById( 'cb-clinic' );
	if ( ! root ) { return; }

	function esc( s ) { var d = document.createElement( 'div' ); d.textContent = s == null ? '' : s; return d.innerHTML; }
	function pane( name ) { return root.querySelector( '.cb-tabpane[data-pane="' + name + '"]' ); }
	function api( path, opts ) {
		opts = opts || {};
		opts.credentials = 'same-origin';
		opts.headers = Object.assign( { 'X-WP-Nonce': cfg.nonce }, opts.headers || {} );
		return fetch( cfg.restUrl + path, opts ).then( function ( r ) { return r.json(); } );
	}
	function post( path, body ) {
		return api( path, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify( body || {} ) } );
	}

	// Tabs.
	Array.prototype.forEach.call( root.querySelectorAll( '.cb-tab' ), function ( t ) {
		t.addEventListener( 'click', function () {
			Array.prototype.forEach.call( root.querySelectorAll( '.cb-tab' ), function ( x ) { x.classList.remove( 'is-on' ); } );
			t.classList.add( 'is-on' );
			var name = t.getAttribute( 'data-tab' );
			Array.prototype.forEach.call( root.querySelectorAll( '.cb-tabpane' ), function ( p ) {
				p.hidden = p.getAttribute( 'data-pane' ) !== name;
			} );
		} );
	} );

	/* Mood */
	function renderMood() {
		var p = pane( 'mood' );
		var faces = [ '😞', '🙁', '😐', '🙂', '😄' ];
		var html = '<div class="cb-mood__pick">';
		faces.forEach( function ( f, i ) { html += '<button type="button" class="cb-mood__face" data-score="' + ( i + 1 ) + '">' + f + '</button>'; } );
		html += '</div><textarea id="cb-mood-note" rows="2" placeholder="یادداشت امروز (اختیاری)…"></textarea>' +
			'<button type="button" class="btn btn--primary" id="cb-mood-save">ثبت حال امروز</button>' +
			'<div id="cb-mood-list" class="cb-mood__list"></div>';
		p.innerHTML = html;
		var chosen = 3;
		Array.prototype.forEach.call( p.querySelectorAll( '.cb-mood__face' ), function ( b ) {
			b.addEventListener( 'click', function () {
				chosen = parseInt( b.getAttribute( 'data-score' ), 10 );
				Array.prototype.forEach.call( p.querySelectorAll( '.cb-mood__face' ), function ( x ) { x.classList.remove( 'is-on' ); } );
				b.classList.add( 'is-on' );
			} );
		} );
		p.querySelector( '#cb-mood-save' ).addEventListener( 'click', function () {
			post( 'mood', { score: chosen, note: p.querySelector( '#cb-mood-note' ).value } ).then( function () {
				p.querySelector( '#cb-mood-note' ).value = '';
				loadMoodList();
			} );
		} );
		loadMoodList();
	}
	function loadMoodList() {
		api( 'mood' ).then( function ( items ) {
			var faces = [ '😞', '🙁', '😐', '🙂', '😄' ];
			var el = pane( 'mood' ).querySelector( '#cb-mood-list' );
			if ( ! items || ! items.length ) { el.innerHTML = '<p class="t-body-sm t-muted">هنوز حالی ثبت نشده.</p>'; return; }
			el.innerHTML = items.slice().reverse().map( function ( m ) {
				return '<div class="cb-mood__row"><span>' + ( faces[ m.score - 1 ] || '😐' ) + '</span><span class="t-body-sm">' +
					esc( m.date ) + '</span><span class="t-body-sm t-muted">' + esc( m.note || '' ) + '</span></div>';
			} ).join( '' );
		} );
	}

	/* Journal */
	function renderJournal() {
		var p = pane( 'journal' );
		p.innerHTML = '<textarea id="cb-j-text" rows="3" placeholder="امروز چه گذشت؟…"></textarea>' +
			'<button type="button" class="btn btn--primary" id="cb-j-save">ثبت</button><div id="cb-j-list" class="cb-list"></div>';
		p.querySelector( '#cb-j-save' ).addEventListener( 'click', function () {
			var ta = p.querySelector( '#cb-j-text' );
			if ( ! ta.value.trim() ) { return; }
			post( 'journal', { text: ta.value } ).then( function () { ta.value = ''; loadJournal(); } );
		} );
		loadJournal();
	}
	function loadJournal() {
		api( 'journal' ).then( function ( items ) {
			var el = pane( 'journal' ).querySelector( '#cb-j-list' );
			if ( ! items || ! items.length ) { el.innerHTML = '<p class="t-body-sm t-muted">ژورنال خالی است.</p>'; return; }
			el.innerHTML = items.map( function ( j ) {
				return '<div class="card card--pad"><p class="t-body">' + esc( j.text ) + '</p><span class="t-body-sm t-muted">' + esc( ( j.time || '' ).slice( 0, 10 ) ) + '</span></div>';
			} ).join( '' );
		} );
	}

	/* Homework */
	function renderHomework() {
		var p = pane( 'homework' );
		p.innerHTML = '<div style="display:flex;gap:8px"><input type="text" id="cb-hw-text" placeholder="تمرین جدید…" style="flex:1">' +
			'<button type="button" class="btn btn--primary" id="cb-hw-add">افزودن</button></div><div id="cb-hw-list" class="cb-list"></div>';
		p.querySelector( '#cb-hw-add' ).addEventListener( 'click', function () {
			var inp = p.querySelector( '#cb-hw-text' );
			if ( ! inp.value.trim() ) { return; }
			post( 'homework', { text: inp.value } ).then( function ( items ) { inp.value = ''; paintHomework( items ); } );
		} );
		api( 'homework' ).then( paintHomework );
	}
	function paintHomework( items ) {
		var el = pane( 'homework' ).querySelector( '#cb-hw-list' );
		if ( ! items || ! items.length ) { el.innerHTML = '<p class="t-body-sm t-muted">تمرینی ثبت نشده.</p>'; return; }
		el.innerHTML = items.map( function ( h, i ) {
			return '<label class="cb-hw__row"><input type="checkbox" data-i="' + i + '"' + ( h.done ? ' checked' : '' ) +
				'><span class="' + ( h.done ? 'cb-hw__done' : '' ) + '">' + esc( h.text ) + '</span></label>';
		} ).join( '' );
		Array.prototype.forEach.call( el.querySelectorAll( 'input[type="checkbox"]' ), function ( c ) {
			c.addEventListener( 'change', function () { post( 'homework/' + c.getAttribute( 'data-i' ) + '/toggle', {} ).then( paintHomework ); } );
		} );
	}

	renderMood();
	renderJournal();
	renderHomework();
} )();
