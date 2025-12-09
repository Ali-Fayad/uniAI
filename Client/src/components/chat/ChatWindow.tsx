import React, { useState } from "react";

type ChatItem = {
  id: string;
  title: string;
  preview?: string;
  lastUpdated?: string;
};

type Message = {
  id: string;
  author: "user" | "bot";
  text: string;
};

type Props = {
  chat: ChatItem | null;
};

const ChatWindow: React.FC<Props> = ({ chat }) => {
  // demo messages
  const [messages, setMessages] = useState<Message[]>([
    {
      id: "m1",
      author: "user",
      text: "Hey uniAI, can you explain the main impacts of Artificial Intelligence on higher education?",
    },
    {
      id: "m2",
      author: "bot",
      text: "Of course! AI is profoundly reshaping higher education. Positive impacts include personalized learning, enhanced research and administrative efficiency. Negative aspects include academic integrity concerns, job displacement, and algorithmic bias.",
    },
  ]);

  const [input, setInput] = useState("");
  const [sending, setSending] = useState(false);

  const sendMessage = async () => {
    if (!input.trim()) return;
    const userMsg: Message = {
      id: String(Date.now()),
      author: "user",
      text: input.trim(),
    };
    setMessages((m) => [...m, userMsg]);
    setInput("");
    setSending(true);

    // demo bot response
    setTimeout(() => {
      setMessages((m) => [
        ...m,
        {
          id: String(Date.now() + 1),
          author: "bot",
          text: "Thanks — here's a short follow-up based on your question (demo).",
        },
      ]);
      setSending(false);
    }, 700);
  };

  return (
    <div className="flex flex-1 flex-col">
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-[#151514]">
          {chat ? chat.title : "Select a conversation"}
        </h2>
        {chat?.lastUpdated && (
          <p className="text-sm text-[#797672] mt-1">Last updated: {chat.lastUpdated}</p>
        )}
      </div>

      <div className="flex-1 space-y-4 overflow-y-auto pr-2">
        {messages.map((m) => (
          <div
            key={m.id}
            className={`max-w-[85%] ${m.author === "user" ? "ml-auto" : "mr-auto"} `}
          >
            <div
              className={`rounded-lg p-4 ${
                m.author === "user" ? "bg-custom-primary/40 text-[#151514]" : "bg-white shadow-sm"
              }`}
            >
              <p className="text-base leading-relaxed">{m.text}</p>
            </div>
          </div>
        ))}
      </div>

      <div className="border-t border-custom-secondary/50 bg-white/50 p-4">
        <div className="mx-auto max-w-4xl">
          <div className="relative">
            <input
              className="w-full rounded-full border-0 bg-white py-3 pl-5 pr-32 text-[#151514] shadow-sm ring-1 ring-inset ring-custom-secondary/80 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-custom-primary"
              placeholder="Type your message here..."
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter" && !e.shiftKey) {
                  e.preventDefault();
                  sendMessage();
                }
              }}
            />
            <div className="absolute inset-y-0 right-0 flex items-center pr-3">
              <button
                onClick={sendMessage}
                disabled={sending}
                className="rounded-full bg-custom-primary p-2 text-white hover:bg-[#a69d8f] transition"
              >
                <span className="material-symbols-outlined">arrow_upward</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ChatWindow;
