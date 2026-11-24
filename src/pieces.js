// Original vector pieces, using common geometric chess silhouettes.
const shapes={
 p:'<circle cx="24" cy="13" r="7"/><path d="M18 20H30L31 29Q33 33 36 35L38 40H10L12 35Q15 33 17 29Z"/>',
 r:'<path d="M10 7H16V13H21V7H27V13H32V7H38V19L33 23L32 32L38 37V41H10V37L16 32L15 23L10 19Z"/><path d="M16 23H32M16 32H32" fill="none"/>',
 n:'<path d="M11 40L13 35L19 29L22 23L16 25L9 20L20 8L20 4L26 8Q36 10 38 20L35 32L39 37V41H11Z"/><circle cx="23" cy="15" r="1.6" fill="var(--line)"/><path d="M14 20L18 21" fill="none"/>',
 b:'<path d="M24 5Q39 16 32 24L28 28L30 33L37 37V41H11V37L18 33L20 28L16 24Q9 16 24 5Z"/><path d="M23 12L29 20M17 29H31" fill="none"/>',
 q:'<path d="M8 14L16 29L14 34L10 38V41H38V38L34 34L32 29L40 14L30 23L24 8L18 23Z"/><circle cx="8" cy="12" r="3"/><circle cx="24" cy="7" r="3"/><circle cx="40" cy="12" r="3"/><path d="M16 29H32M14 34H34" fill="none"/>',
 k:'<path d="M21 4H27V9H32V14H27V18Q37 12 39 23L32 32L37 37V41H11V37L16 32L9 23Q11 12 21 18V14H16V9H21Z"/><path d="M16 32H32M24 18V29" fill="none"/>'
};
export function pieceSVG(p){const w=p.color==='w';return '<svg class="piece" viewBox="0 0 48 48" aria-hidden="true" xmlns="http://www.w3.org/2000/svg"><g fill="'+(w?'#fff9e9':'#25342d')+'" stroke="'+(w?'#26392f':'#0b1710')+'" stroke-width="1.7" stroke-linejoin="round" stroke-linecap="round">'+shapes[p.type]+'</g></svg>';}
