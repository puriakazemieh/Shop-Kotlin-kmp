/* Shopping assistant: category → budget → sort → matched products. */
( function () {
	'use strict';
	var cfg = window.CarmillaData || {};
	var root = document.getElementById( 'cb-assistant' );
	if ( ! root ) { return; }
	var stepEl = document.getElementById( 'cb-assistant-step' );
	var resultsEl = document.getElementById( 'cb-assistant-results' );
	var cats = [];
	try { cats = JSON.parse( root.getAttribute( 'data-cats' ) || '[]' ); } catch ( e ) {}

	var state = { cat: 0, max: 0, sort: 'date' };
	var step = 0;

	function esc( s ) { var d = document.createElement( 'div' ); d.textContent = s == null ? '' : s; return d.innerHTML; }

	var budgets = [
		{ label: 'تا ۵۰۰ هزار تومان', max: 500000 },
		{ label: '۵۰۰ هزار تا ۲ میلیون', max: 2000000 },
		{ label: 'بیش از ۲ میلیون', max: 0 }
	];
	var sorts = [
		{ label: 'جدیدترین', v: 'date' },
		{ label: 'ارزان‌ترین', v: 'price_asc' },
		{ label: 'محبوب‌ترین', v: 'rating' }
	];

	function chips( title, items, onPick ) {
		var html = '<h3 class="t-title-sm">' + esc( title ) + '</h3><div class="cb-assistant__chips">';
		items.forEach( function ( it, i ) {
			html += '<button type="button" class="chip" data-i="' + i + '">' + esc( it.label ) + '</button>';
		} );
		html += '</div>';
		stepEl.innerHTML = html;
		Array.prototype.forEach.call( stepEl.querySelectorAll( 'button' ), function ( b ) {
			b.addEventListener( 'click', function () { onPick( parseInt( b.getAttribute( 'data-i' ), 10 ) ); } );
		} );
	}

	function render() {
		if ( step === 0 ) {
			var catItems = [ { label: 'همه‌ی دسته‌ها', id: 0 } ].concat( cats.map( function ( c ) { return { label: c.name, id: c.id }; } ) );
			chips( 'دنبال چه محصولی هستید؟', catItems, function ( i ) { state.cat = catItems[ i ].id; step = 1; render(); } );
		} else if ( step === 1 ) {
			chips( 'بودجه‌ی شما؟', budgets, function ( i ) { state.max = budgets[ i ].max; step = 2; render(); } );
		} else if ( step === 2 ) {
			chips( 'مرتب‌سازی بر اساس؟', sorts, function ( i ) { state.sort = sorts[ i ].v; step = 3; fetchResults(); } );
		}
	}

	function fetchResults() {
		stepEl.innerHTML = '<p class="t-body t-muted">در حال یافتن بهترین گزینه‌ها…</p>';
		var url = cfg.restUrl + 'assistant?cat=' + state.cat + '&max=' + state.max + '&sort=' + encodeURIComponent( state.sort );
		fetch( url, { credentials: 'same-origin' } )
			.then( function ( r ) { return r.json(); } )
			.then( function ( items ) {
				stepEl.innerHTML = '<button type="button" id="cb-assistant-restart" class="btn btn--ghost">جست‌وجوی دوباره</button>';
				document.getElementById( 'cb-assistant-restart' ).addEventListener( 'click', function () {
					step = 0; resultsEl.innerHTML = ''; render();
				} );
				if ( ! items || ! items.length ) {
					resultsEl.innerHTML = '<p class="t-body t-muted">موردی یافت نشد؛ فیلترها را تغییر دهید.</p>';
					return;
				}
				resultsEl.innerHTML = items.map( function ( it ) {
					return '<article class="card product-card"><a href="' + esc( it.permalink ) + '" class="thumb"><img src="' + esc( it.image ) + '" alt=""></a>' +
						'<div class="body"><h3 class="t-title-sm"><a href="' + esc( it.permalink ) + '">' + esc( it.name ) + '</a></h3>' +
						'<div class="price-row" style="margin-block-start:6px"><span class="price">' + it.priceHtml + '</span></div></div></article>';
				} ).join( '' );
			} );
	}

	render();
} )();
