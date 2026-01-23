import React from 'react';

interface LayoutProps {
  children: React.ReactNode;
}

export function Layout({ children }: LayoutProps) {
  return (
    <div className="min-h-screen flex flex-col bg-white">
      {/* Promo Bar */}
      <div className="promo-bar">
        Darmowa dostawa od 150 PLN | 30 dni na zwrot
      </div>

      {/* Header */}
      <header className="border-bottom border-gray-100 py-4 px-6 md:px-12 flex justify-between items-center bg-white sticky top-0 z-50 shadow-sm">
        <div className="flex items-center gap-8">
          <img
            src="https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Sinsay_logo.svg/2560px-Sinsay_logo.svg.png"
            alt="Sinsay"
            className="h-6 md:h-8"
          />
        </div>

        <div className="flex items-center gap-6">
          <div className="hidden md:flex items-center bg-gray-100 rounded-full px-4 py-2 w-64">
            <svg
              className="w-4 h-4 text-gray-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
              />
            </svg>
            <input
              type="text"
              placeholder="Szukaj"
              className="bg-transparent border-none focus:ring-0 text-sm ml-2 w-full outline-none"
            />
          </div>
          <div className="flex items-center gap-4">
            <svg
              className="w-6 h-6 text-black cursor-pointer"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="1.5"
                d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
              />
            </svg>
            <div className="relative cursor-pointer">
              <svg
                className="w-6 h-6 text-black"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="1.5"
                  d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"
                />
              </svg>
            </div>
          </div>
        </div>
      </header>

      <main className="flex-1 max-w-4xl mx-auto w-full p-4 md:p-8">
        {children}
      </main>

      <footer className="bg-black text-white py-12 px-6 md:px-12">
        <div className="max-w-6xl mx-auto grid grid-cols-1 md:grid-cols-4 gap-8">
          <div>
            <h4 className="text-white font-bold mb-4 uppercase text-sm tracking-widest">
              Pomoc i kontakt
            </h4>
            <ul className="space-y-2 text-xs text-gray-400">
              <li>Jak kupować</li>
              <li>Koszty dostawy</li>
              <li>Zwroty i reklamacje</li>
              <li>Tabela rozmiarów</li>
            </ul>
          </div>
          <div>
            <h4 className="text-white font-bold mb-4 uppercase text-sm tracking-widest">
              Polityka prywatności
            </h4>
            <ul className="space-y-2 text-xs text-gray-400">
              <li>Polityka prywatności</li>
              <li>Polityka cookies</li>
              <li>Ustawienia cookies</li>
            </ul>
          </div>
          <div>
            <h4 className="text-white font-bold mb-4 uppercase text-sm tracking-widest">
              O Sinsay
            </h4>
            <ul className="space-y-2 text-xs text-gray-400">
              <li>O nas</li>
              <li>Pressroom</li>
              <li>Kariera</li>
            </ul>
          </div>
          <div>
            <h4 className="text-white font-bold mb-4 uppercase text-sm tracking-widest">
              Newsletter
            </h4>
            <p className="text-xs text-gray-400 mb-4">
              Zapisz się i otrzymaj -15% na pierwsze zakupy!
            </p>
            <div className="flex gap-2">
              <input
                type="email"
                placeholder="E-mail"
                className="bg-white text-black p-2 text-xs w-full outline-none"
              />
              <button className="bg-white text-black px-4 py-2 text-xs font-bold font-montserrat">
                OK
              </button>
            </div>
          </div>
        </div>
        <div className="mt-12 pt-8 border-t border-gray-800 text-center text-[10px] text-gray-500 uppercase tracking-widest">
          © 2026 Sinsay | SilkyCodders PoC
        </div>
      </footer>
    </div>
  );
}
