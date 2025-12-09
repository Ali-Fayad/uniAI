import React, { useState } from "react";
import NavBar from "../components/chat/ChatNavBar";
import Sidebar from "../components/chat/ChatSideBar";
import ChatWindow from "../components/chat/ChatWindow.tsx";

type ChatItem = {
  id: string;
  title: string;
  preview?: string;
  lastUpdated?: string;
};

const ChatPage: React.FC = () => {
  const [selectedChat, setSelectedChat] = useState<ChatItem | null>(null);

  return (
    <div className="flex h-screen w-full flex-col bg-custom-light">
      <NavBar />
      <div className="flex flex-1 overflow-hidden">
        <aside className="w-80 flex-shrink-0 border-r border-custom-secondary/50 bg-white/50 p-4">
          <Sidebar
            onSelect={(c) => {
              setSelectedChat(c);
            }}
          />
        </aside>

        <main className="flex flex-1 flex-col">
          <div className="flex-1 overflow-y-auto p-6 lg:p-8">
            <div className="mx-auto max-w-4xl">
              <ChatWindow chat={selectedChat} />
            </div>
          </div>
        </main>
      </div>
    </div>
  );
};

export default ChatPage;
