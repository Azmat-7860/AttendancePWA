import React from 'react';

/**
 * Full-screen atmospheric construction site illustration:
 * - Seamless dusk sky backdrop spanning the entire viewport
 * - Tall latticed tower crane & unfinished building frames rising behind the glass login card
 * - Foreground bottom: Realistic Worker with axe tapping attendance checkbox, Owner surveying sites, Supervisor marking register
 */
export const SiteIllustrationScene: React.FC = () => {
  return (
    <div
      className="absolute inset-0 z-0 pointer-events-none select-none overflow-hidden bg-gradient-to-b from-slate-950 via-[#0f172a] to-[#1e293b]"
      aria-hidden="true"
    >
      {/* Full-Viewport Ambient Dusk & Emerald Glows */}
      <div className="absolute -top-24 left-1/2 -translate-x-1/2 w-[520px] h-72 rounded-full bg-emerald-500/10 blur-3xl" />
      <div className="absolute bottom-12 left-1/3 w-96 h-64 rounded-full bg-amber-500/15 blur-3xl" />

      {/* Full-Height Transparent SVG Scene anchored to bottom */}
      <svg
        viewBox="0 0 380 520"
        preserveAspectRatio="xMidYMax meet"
        className="w-full h-full max-w-3xl mx-auto block"
      >
        <defs>
          {/* Realistic Skin & Shading Gradients */}
          <linearGradient id="skinLight" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor="#eab692" />
            <stop offset="100%" stopColor="#c98863" />
          </linearGradient>
          <linearGradient id="skinDeep" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#c98863" />
            <stop offset="100%" stopColor="#9d6140" />
          </linearGradient>

          {/* Clothing Gradients */}
          <linearGradient id="vestOrange" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#fb923c" />
            <stop offset="100%" stopColor="#c2410c" />
          </linearGradient>
          <linearGradient id="shirtBlue" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor="#60a5fa" />
            <stop offset="60%" stopColor="#2563eb" />
            <stop offset="100%" stopColor="#1e3a8a" />
          </linearGradient>
          <linearGradient id="ownerCoat" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#475569" />
            <stop offset="100%" stopColor="#1e293b" />
          </linearGradient>
          <linearGradient id="helmetYellow" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#fde047" />
            <stop offset="100%" stopColor="#ca8a04" />
          </linearGradient>
          <linearGradient id="helmetWhite" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#ffffff" />
            <stop offset="100%" stopColor="#cbd5e1" />
          </linearGradient>
          <linearGradient id="axeWood" x1="0" y1="0" x2="1" y2="0">
            <stop offset="0%" stopColor="#b45309" />
            <stop offset="50%" stopColor="#92400e" />
            <stop offset="100%" stopColor="#78350f" />
          </linearGradient>
          <linearGradient id="axeSteel" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor="#f1f5f9" />
            <stop offset="55%" stopColor="#94a3b8" />
            <stop offset="100%" stopColor="#475569" />
          </linearGradient>
          <linearGradient id="groundGrad" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#1e293b" />
            <stop offset="100%" stopColor="#020617" />
          </linearGradient>
          <linearGradient id="buildingFade" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#0f172a" stopOpacity="0.3" />
            <stop offset="100%" stopColor="#0f172a" stopOpacity="0.92" />
          </linearGradient>
        </defs>

        {/* FULL-SCREEN UPPER SKYLINE: Tall Buildings & Crane rising behind the middle Glass Box */}
        <g opacity="0.65">
          {/* Left Multi-Storey Under-Construction Tower */}
          <rect x="14" y="155" width="46" height="345" fill="url(#buildingFade)" stroke="#334155" strokeWidth="0.7" />
          <line x1="12" y1="185" x2="62" y2="185" stroke="#475569" strokeWidth="1.2" />
          <line x1="12" y1="225" x2="62" y2="225" stroke="#475569" strokeWidth="1.2" />
          <line x1="12" y1="265" x2="62" y2="265" stroke="#475569" strokeWidth="1.2" />
          <line x1="12" y1="305" x2="62" y2="305" stroke="#475569" strokeWidth="1.2" />
          <line x1="12" y1="345" x2="62" y2="345" stroke="#475569" strokeWidth="1.2" />
          {/* Top Rebar */}
          <line x1="22" y1="143" x2="22" y2="155" stroke="#64748b" strokeWidth="1" />
          <line x1="37" y1="141" x2="37" y2="155" stroke="#64748b" strokeWidth="1" />
          <line x1="52" y1="144" x2="52" y2="155" stroke="#64748b" strokeWidth="1" />

          {/* Tall Center-Left Core Tower */}
          <rect x="68" y="115" width="52" height="385" fill="url(#buildingFade)" stroke="#334155" strokeWidth="0.7" />
          <g fill="#fbbf24" opacity="0.28">
            <rect x="78" y="135" width="8" height="5" rx="0.5" />
            <rect x="96" y="135" width="8" height="5" rx="0.5" />
            <rect x="78" y="160" width="8" height="5" rx="0.5" />
            <rect x="96" y="160" width="8" height="5" rx="0.5" />
            <rect x="78" y="185" width="8" height="5" rx="0.5" />
          </g>

          {/* Right Stepped Structure + Scaffolding */}
          <rect x="266" y="165" width="52" height="335" fill="url(#buildingFade)" stroke="#334155" strokeWidth="0.7" />
          <rect x="322" y="210" width="44" height="290" fill="url(#buildingFade)" stroke="#334155" strokeWidth="0.7" />
          <g stroke="#475569" strokeWidth="0.6" opacity="0.5">
            <line x1="276" y1="165" x2="276" y2="500" />
            <line x1="298" y1="165" x2="298" y2="500" />
            <line x1="266" y1="205" x2="318" y2="205" />
            <line x1="266" y1="245" x2="318" y2="245" />
            <line x1="266" y1="285" x2="318" y2="285" />
          </g>

          {/* Tall Latticed Tower Crane rising high above the Login Box */}
          <line x1="155" y1="88" x2="155" y2="500" stroke="#475569" strokeWidth="1.5" />
          <line x1="161" y1="88" x2="161" y2="500" stroke="#475569" strokeWidth="1.5" />
          <path
            d="M155 490 L161 470 L155 450 L161 430 L155 410 L161 390 L155 370 L161 350 L155 330 L161 310 L155 290 L161 270 L155 250 L161 230 L155 210 L161 190 L155 170 L161 150 L155 130 L161 110 L155 94"
            fill="none"
            stroke="#64748b"
            strokeWidth="0.75"
          />
          <rect x="150" y="94" width="12" height="9" rx="1" fill="#334155" stroke="#64748b" strokeWidth="0.6" />
          <rect x="156" y="96.5" width="4.5" height="3.5" fill="#fde68a" opacity="0.75" />

          {/* Slewing Jib Assembly near Upper Sky */}
          <g transform="translate(0, 66)">
            <g className="anim-crane-sway">
              <polygon points="154,22 158,8 162,22" fill="#f59e0b" />
              <line x1="158" y1="8" x2="90" y2="21" stroke="#94a3b8" strokeWidth="0.8" />
              <line x1="158" y1="8" x2="262" y2="21" stroke="#94a3b8" strokeWidth="0.8" />
              <rect x="86" y="21" width="180" height="4" rx="0.8" fill="#d97706" />
              <line x1="86" y1="21" x2="266" y2="21" stroke="#fbbf24" strokeWidth="0.8" />
              <rect x="90" y="25" width="16" height="8" rx="0.8" fill="#64748b" stroke="#334155" strokeWidth="0.6" />
              <rect x="226" y="25" width="8" height="3.2" fill="#e2e8f0" />
              <line x1="230" y1="28" x2="230" y2="62" stroke="#cbd5e1" strokeWidth="0.8" />
              <path d="M228.5 62 C228.5 65, 231.5 65, 231.5 62" fill="none" stroke="#f59e0b" strokeWidth="1" />
              <rect x="223" y="65" width="14" height="3.8" rx="0.5" fill="#10b981" />
            </g>
          </g>
        </g>

        {/* FOREGROUND BOTTOM SCENE: Ground + Owner + Worker + Supervisor */}
        <g transform="translate(0, 335)">
          {/* Ground Asphalt & Concrete Foreground */}
          <rect x="-200" y="165" width="780" height="40" fill="url(#groundGrad)" />
          <line x1="-200" y1="165" x2="580" y2="165" stroke="#475569" strokeWidth="1" />

          {/* CENTER MID-GROUND: Realistic Small Owner Looking Over the Sites */}
          <g className="anim-breathe">
            <rect x="174" y="142" width="72" height="5" rx="1" fill="#334155" stroke="#475569" strokeWidth="0.7" />
            <line x1="180" y1="147" x2="180" y2="165" stroke="#334155" strokeWidth="3" />
            <line x1="238" y1="147" x2="238" y2="165" stroke="#334155" strokeWidth="3" />
            <line x1="176" y1="125" x2="244" y2="125" stroke="#64748b" strokeWidth="1.1" />
            <line x1="176" y1="133" x2="244" y2="133" stroke="#475569" strokeWidth="0.8" />
            <line x1="177" y1="125" x2="177" y2="142" stroke="#64748b" strokeWidth="1.2" />
            <line x1="202" y1="125" x2="202" y2="142" stroke="#64748b" strokeWidth="1.2" />
            <line x1="226" y1="125" x2="226" y2="142" stroke="#64748b" strokeWidth="1.2" />
            <line x1="243" y1="125" x2="243" y2="142" stroke="#64748b" strokeWidth="1.2" />
            <ellipse cx="209" cy="142" rx="9" ry="1.8" fill="#020617" opacity="0.55" />

            <path d="M204 118 L203 139 L207.5 139 L208.5 118 Z" fill="#0f172a" />
            <path d="M209.5 118 L210.5 139 L215 139 L214 118 Z" fill="#1e293b" />
            <path d="M201.5 138.5 L208 138.5 L208 141.5 L201 141.5 Z" fill="#020617" />
            <path d="M210 138.5 L216.5 138.5 L216.5 141.5 L210 141.5 Z" fill="#020617" />

            <path d="M201 98 Q209 95.5 216 98 L217 119 L200 119 Z" fill="url(#ownerCoat)" />
            <polygon points="205.5,97 211,97 208,106" fill="#f8fafc" />
            <path d="M202 100 L196 109 L202 112" fill="none" stroke="#334155" strokeWidth="3.6" strokeLinecap="round" strokeLinejoin="round" />
            <circle cx="202" cy="112" r="2" fill="url(#skinLight)" />
            <rect x="189" y="106" width="11.5" height="8" rx="1" fill="#38bdf8" stroke="#0284c7" strokeWidth="0.9" transform="rotate(-15 195 110)" />
            <line x1="192" y1="109" x2="198" y2="108" stroke="#e0f2fe" strokeWidth="0.7" transform="rotate(-15 195 110)" />
            <path d="M215 100 L218 114" fill="none" stroke="#1e293b" strokeWidth="3.5" strokeLinecap="round" />
            <circle cx="218" cy="115" r="1.9" fill="url(#skinDeep)" />
            <rect x="206" y="92" width="4.2" height="5" fill="url(#skinDeep)" />

            <g className="anim-owner-survey">
              <path d="M203 86.5 C202 89.5, 203 93.5, 207.5 94 C211.5 94, 213 91, 213 87 Z" fill="url(#skinLight)" />
              <path d="M203 89 L201.8 90.8 L203.2 91" fill="url(#skinLight)" />
              <path d="M209 86.5 C212 87, 213.5 89.5, 212 92" fill="none" stroke="#1e293b" strokeWidth="1.8" />
              <circle cx="205.2" cy="89" r="0.7" fill="#0f172a" />
              <path d="M201.5 87 C201.5 81, 214.5 81, 214.5 87 Z" fill="url(#helmetWhite)" />
              <path d="M199.5 86.8 L215.5 87.2" stroke="#cbd5e1" strokeWidth="1.6" strokeLinecap="round" />
            </g>
            <rect x="195" y="71" width="27" height="8" rx="4" fill="#6d28d9" opacity="0.88" />
            <text x="208.5" y="77" textAnchor="middle" fill="#ffffff" fontSize="4.6" fontWeight="bold" fontFamily="sans-serif">
              OWNER
            </text>
          </g>

          {/* FOREGROUND LEFT: Realistic Small Worker with Axe Tapping Attendance Checkbox */}
          <g className="anim-breathe">
            <ellipse cx="52" cy="172" rx="18" ry="3.2" fill="#020617" opacity="0.6" />
            <circle cx="118" cy="111" r="12" fill="#10b981" className="anim-tick-ripple" />
            <rect x="103" y="99" width="30" height="26" rx="5.5" fill="#ffffff" stroke="#10b981" strokeWidth="1.6" />
            <rect x="108" y="103" width="20" height="15" rx="3" fill="#ecfdf5" stroke="#a7f3d0" strokeWidth="0.8" />
            <path
              d="M112.5 110.5 L116.5 114.5 L123.5 106.5"
              fill="none"
              stroke="#059669"
              strokeWidth="2.3"
              strokeLinecap="round"
              strokeLinejoin="round"
              className="anim-tick-draw"
            />
            <text x="118" y="122.5" textAnchor="middle" fill="#047857" fontSize="4.6" fontWeight="bold" fontFamily="sans-serif">
              PRESENT
            </text>

            <path d="M43 137 L41 168 L47.5 168 L50 137 Z" fill="#334155" />
            <path d="M52 137 L54.5 168 L61 168 L59 137 Z" fill="#1e293b" />
            <rect x="42" y="151" width="5.5" height="5" rx="0.8" fill="#475569" opacity="0.55" />
            <rect x="54" y="151" width="5.5" height="5" rx="0.8" fill="#334155" opacity="0.55" />
            <path d="M39.5 167 L48.5 167 L49.5 171.5 L39.5 171.5 Z" fill="#451a03" />
            <rect x="39.5" y="170.5" width="10.2" height="1.5" fill="#020617" />
            <path d="M53.5 167 L62.5 167 L64 171.5 L53.5 171.5 Z" fill="#451a03" />
            <rect x="53.5" y="170.5" width="10.7" height="1.5" fill="#020617" />

            <path d="M40 108 Q51 104.5 62 108 L63 137 L39 137 Z" fill="#1e293b" />
            <path d="M41.5 107.5 L47 107.5 L51 114 L55 107.5 L60.5 107.5 L62 136.5 L40 136.5 Z" fill="url(#vestOrange)" />
            <rect x="40" y="121" width="22" height="2.4" fill="#fef08a" opacity="0.92" />
            <rect x="40" y="128.5" width="22" height="2.4" fill="#fef08a" opacity="0.92" />
            <rect x="39.5" y="135.5" width="23" height="2.6" rx="0.5" fill="#0f172a" />

            <path d="M24 98 Q28 122 30 150" fill="none" stroke="url(#axeWood)" strokeWidth="2.8" strokeLinecap="round" />
            <line x1="27.2" y1="120" x2="28.3" y2="128" stroke="#1e293b" strokeWidth="3.1" />
            <path d="M13 94.5 L27 92.5 L27 102.5 L15 104 Z" fill="url(#axeSteel)" stroke="#334155" strokeWidth="0.6" />
            <path d="M13 94.5 L15 104" stroke="#ffffff" strokeWidth="1.1" strokeLinecap="round" />
            <path d="M41 109 L34 119 L28 124" fill="none" stroke="#334155" strokeWidth="4.4" strokeLinecap="round" strokeLinejoin="round" />
            <circle cx="27.5" cy="124.5" r="2.6" fill="url(#skinLight)" />

            <g className="anim-worker-tap">
              <path d="M60 109 L76 116 L97 112" fill="none" stroke="#334155" strokeWidth="4.2" strokeLinecap="round" strokeLinejoin="round" />
              <path d="M84 114.5 L98 111.5" fill="none" stroke="url(#skinLight)" strokeWidth="3.4" strokeLinecap="round" />
              <line x1="98" y1="111.5" x2="103.5" y2="110.5" stroke="url(#skinLight)" strokeWidth="1.6" strokeLinecap="round" />
            </g>

            <rect x="48.5" y="101" width="4.8" height="6" fill="url(#skinDeep)" />
            <path d="M45.5 92.5 C45.5 98, 47.5 102, 52 102 C56 102, 57.5 98.5, 57.2 92.5 Z" fill="url(#skinLight)" />
            <path d="M57 95 L58.4 97 L56.8 97.5" fill="url(#skinLight)" />
            <circle cx="46.2" cy="96" r="1.4" fill="url(#skinDeep)" />
            <path d="M45.5 92.5 C45 94, 45 96, 46.5 96.5" stroke="#1e293b" strokeWidth="1.6" fill="none" />
            <line x1="52.5" y1="93.8" x2="55.2" y2="94.1" stroke="#334155" strokeWidth="0.7" />
            <circle cx="54" cy="95.4" r="0.75" fill="#0f172a" />
            <line x1="53.5" y1="99.2" x2="55.8" y2="99" stroke="#7c2d12" strokeWidth="0.7" strokeLinecap="round" />
            <path d="M44 92.5 C44 84, 59 84, 59 92.5 Z" fill="url(#helmetYellow)" />
            <path d="M50 85.2 L53 85.2 L52.5 91 L50.5 91 Z" fill="#eab308" />
            <path d="M43 92 L62 92.8" stroke="#ca8a04" strokeWidth="1.9" strokeLinecap="round" />
          </g>

          {/* FOREGROUND RIGHT: Realistic Small Supervisor Marking Attendance in Register */}
          <g className="anim-breathe">
            <ellipse cx="322" cy="172" rx="18" ry="3.2" fill="#020617" opacity="0.6" />
            <path d="M312 137 L310 168 L316.5 168 L319 137 Z" fill="#0f172a" />
            <path d="M321 137 L323.5 168 L330 168 L328 137 Z" fill="#1e293b" />
            <path d="M308 167.5 L317 167.5 L317 171.5 L307 171.5 Z" fill="#020617" />
            <path d="M322.5 167.5 L331.5 167.5 L332.5 171.5 L322.5 171.5 Z" fill="#020617" />

            <path d="M310 108 Q321 104.5 332 108 L333 137 L309 137 Z" fill="url(#shirtBlue)" />
            <polygon points="316,106.5 321,113.5 326,106.5" fill="#dbeafe" />
            <line x1="321" y1="113.5" x2="321" y2="137" stroke="#1e3a8a" strokeWidth="0.7" />
            <rect x="323.5" y="116" width="5.5" height="6" rx="0.6" fill="#1d4ed8" stroke="#93c5fd" strokeWidth="0.4" />
            <rect x="309.5" y="135.5" width="23" height="2.4" fill="#020617" />
            <rect x="319" y="135.5" width="3" height="2.4" fill="#cbd5e1" />

            <g transform="translate(274, 110) rotate(-7)">
              <rect x="0" y="0" width="27" height="34" rx="2" fill="#451a03" stroke="#78350f" strokeWidth="0.7" />
              <rect x="2.5" y="3.5" width="22" height="27.5" rx="1" fill="#ffffff" />
              <rect x="9" y="-1.5" width="9" height="4" rx="1" fill="#94a3b8" />
              <line x1="7" y1="5" x2="7" y2="29" stroke="#fca5a5" strokeWidth="0.6" />
              <line x1="4" y1="9" x2="18" y2="9" stroke="#cbd5e1" strokeWidth="1" />
              <path d="M19.5 8 L21 9.5 L23 7" fill="none" stroke="#10b981" strokeWidth="1.2" strokeLinecap="round" />
              <line x1="4" y1="14.5" x2="18" y2="14.5" stroke="#cbd5e1" strokeWidth="1" />
              <path d="M19.5 13.5 L21 15 L23 12.5" fill="none" stroke="#10b981" strokeWidth="1.2" strokeLinecap="round" />
              <line x1="4" y1="20" x2="18" y2="20" stroke="#cbd5e1" strokeWidth="1" />
              <path d="M19.5 19 L21 20.5 L23 18" fill="none" stroke="#10b981" strokeWidth="1.2" strokeLinecap="round" />
              <line x1="4" y1="25.5" x2="18" y2="25.5" stroke="#cbd5e1" strokeWidth="1" />
            </g>
            <path d="M312 114 L305 134 L292 140" fill="none" stroke="#1d4ed8" strokeWidth="3.8" strokeLinecap="round" strokeLinejoin="round" />
            <circle cx="291" cy="140" r="2.2" fill="url(#skinDeep)" />

            <g className="anim-supervisor-write">
              <path d="M314 109 L304 123 L293 125" fill="none" stroke="#3b82f6" strokeWidth="4.1" strokeLinecap="round" strokeLinejoin="round" />
              <path d="M299 124 L292 125" fill="none" stroke="url(#skinLight)" strokeWidth="3.2" strokeLinecap="round" />
              <circle cx="291" cy="125" r="2.3" fill="url(#skinLight)" />
              <line x1="291.5" y1="124.5" x2="285.5" y2="128" stroke="#0f172a" strokeWidth="1.5" strokeLinecap="round" />
              <circle cx="285.3" cy="128.2" r="0.6" fill="#2563eb" />
            </g>

            <rect x="319" y="101" width="4.8" height="6" fill="url(#skinDeep)" />
            <path d="M314.8 92.5 C314.5 98.5, 316.5 102, 321 102 C325 102, 326.5 98, 326.5 92.5 Z" fill="url(#skinLight)" />
            <path d="M315 95.5 L313.5 97.4 L315.2 97.8" fill="url(#skinLight)" />
            <circle cx="325.5" cy="96" r="1.4" fill="url(#skinDeep)" />
            <path d="M326 92.5 C326.8 94.5, 326.5 96.5, 325 97" stroke="#0f172a" strokeWidth="1.6" fill="none" />
            <line x1="316.5" y1="94" x2="319.5" y2="94.5" stroke="#334155" strokeWidth="0.7" />
            <circle cx="318" cy="96" r="0.75" fill="#0f172a" />
            <line x1="316.5" y1="99.3" x2="318.8" y2="99.1" stroke="#7c2d12" strokeWidth="0.7" strokeLinecap="round" />
            <path d="M313.5 92.5 C313.5 84, 328.5 84, 328.5 92.5 Z" fill="url(#helmetWhite)" />
            <path d="M318.5 85 L322.5 85 L322 91 L319 91 Z" fill="#38bdf8" />
            <path d="M311 92.8 L329.5 92" stroke="#0284c7" strokeWidth="1.9" strokeLinecap="round" />
          </g>
        </g>
      </svg>
    </div>
  );
};
