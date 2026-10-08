import {mkdirSync, writeFileSync} from 'node:fs';
const icons = {
  search: 'M17 10a7 7 0 1 1-14 0 7 7 0 0 1 14 0M15 15l6 6',
  share: 'M6 12L18 5M6 12L18 19M8.5 12a2.5 2.5 0 1 1-5 0 2.5 2.5 0 0 1 5 0M20.5 5a2.5 2.5 0 1 1-5 0 2.5 2.5 0 0 1 5 0M20.5 19a2.5 2.5 0 1 1-5 0 2.5 2.5 0 0 1 5 0',
  bookmark: 'M6 3h12v18l-6-4-6 4z',
  speaker: 'M3 9h5l5-5v16l-5-5H3zM17 5a9 9 0 0 1 0 14',
  arrow: 'M4 12h16M14 6l6 6-6 6',
  back: 'M20 12H4M10 6l-6 6 6 6',
  close: 'M6 6l12 12M6 18L18 6',
  sun: 'M16 12a4 4 0 1 1-8 0 4 4 0 0 1 8 0M12 2v2M12 20v2M2 12h2M20 12h2M5 5l1.5 1.5M17.5 17.5L19 19M5 19l1.5-1.5M17.5 6.5L19 5',
  moon: 'M20 15a8 8 0 1 1-11-11 7 7 0 0 0 11 11z',
  system: 'M12 3a9 9 0 1 1 0 18 9 9 0 0 1 0-18M12 3v18M12 7a5 5 0 0 1 0 10',
  settings: 'M10 2h4l.5 3 2.3 1.3 2.8-1 2 3.4-2.3 2v2.6l2.3 2-2 3.4-2.8-1-2.3 1.3-.5 3h-4l-.5-3-2.3-1.3-2.8 1-2-3.4 2.3-2v-2.6l-2.3-2 2-3.4 2.8 1L9.5 5zM15.5 12a3.5 3.5 0 1 1-7 0 3.5 3.5 0 0 1 7 0',
};
mkdirSync('app/src/main/assets/icons', {recursive:true});
mkdirSync('app/src/main/res/animator', {recursive:true});
writeFileSync('app/src/main/res/animator/icon_press.xml', '<objectAnimator xmlns:android="http://schemas.android.com/apk/res/android" android:propertyName="rotation" android:valueFrom="0" android:valueTo="6" android:duration="160" android:valueType="floatType" android:interpolator="@android:interpolator/fast_out_slow_in"/>\n');
for (const [name,path] of Object.entries(icons)) {
  writeFileSync(`app/src/main/assets/icons/${name}.svg`, `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="${path}" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/></svg>\n`);
  const vector = (fill = '#00000000') => `<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24" android:autoMirrored="${name === 'arrow' || name === 'back'}"><group android:name="icon" android:pivotX="12" android:pivotY="12"><path android:pathData="${path}" android:fillColor="${fill}" android:strokeColor="#FF000000" android:strokeWidth="1.7" android:strokeLineCap="round" android:strokeLineJoin="round"/></group></vector>\n`;
  writeFileSync(`app/src/main/res/drawable/vec_${name}.xml`, vector());
  if (name === 'bookmark') writeFileSync('app/src/main/res/drawable/vec_bookmark_filled.xml',vector('#FF000000'));
  writeFileSync(`app/src/main/res/drawable/anim_${name}.xml`, `<animated-vector xmlns:android="http://schemas.android.com/apk/res/android" android:drawable="@drawable/vec_${name}"><target android:name="icon" android:animation="@animator/icon_press"/></animated-vector>\n`);
}
