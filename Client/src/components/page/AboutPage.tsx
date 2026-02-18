import React from "react";
import { motion } from "framer-motion";
import { PageTransition, StaggerContainer, staggerItemVariants } from "../animations";

/**
 * AboutPage Component
 * 
 * Displays information about the uniAI platform
 */
const AboutPage: React.FC = () => {
  return (
    <PageTransition>
      <main className="min-h-[calc(100vh-64px)] bg-[var(--color-background)] py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-4xl mx-auto">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            className="text-center mb-12"
          >
            <h1 className="text-4xl sm:text-5xl font-bold text-[var(--color-textPrimary)] mb-4">
              About uniAI
            </h1>
            <p className="text-xl text-[var(--color-textSecondary)]">
              Your AI-powered university companion
            </p>
          </motion.div>

          <StaggerContainer staggerDelay={0.1} initialDelay={0.2}>
            <motion.section 
              variants={staggerItemVariants}
              className="bg-[var(--color-surface)] rounded-lg p-8 mb-8 shadow-md border border-[var(--color-border)]"
            >
              <h2 className="text-2xl font-bold text-[var(--color-textPrimary)] mb-4">
                Our Mission
              </h2>
              <p className="text-[var(--color-textSecondary)] leading-relaxed">
                uniAI is dedicated to revolutionizing the university experience by providing 
                students with intelligent tools and resources. We aim to make academic life 
                easier, more organized, and more connected through AI-powered assistance.
              </p>
            </motion.section>

            <motion.section 
              variants={staggerItemVariants}
              className="bg-[var(--color-surface)] rounded-lg p-8 mb-8 shadow-md border border-[var(--color-border)]"
            >
              <h2 className="text-2xl font-bold text-[var(--color-textPrimary)] mb-4">
                Features
              </h2>
              <div className="space-y-4">
                <div className="flex items-start">
                  <span className="material-symbols-outlined text-[var(--color-primary)] mr-3 mt-1">
                    chat
                  </span>
                  <div>
                    <h3 className="font-semibold text-[var(--color-textPrimary)] mb-1">
                      AI Chat Assistant
                    </h3>
                    <p className="text-[var(--color-textSecondary)]">
                      Get instant answers to your academic questions with our intelligent chat system.
                    </p>
                  </div>
                </div>
                <div className="flex items-start">
                  <span className="material-symbols-outlined text-[var(--color-primary)] mr-3 mt-1">
                    map
                  </span>
                  <div>
                    <h3 className="font-semibold text-[var(--color-textPrimary)] mb-1">
                      University Map
                    </h3>
                    <p className="text-[var(--color-textSecondary)]">
                      Explore universities and their locations with our interactive map feature.
                    </p>
                  </div>
                </div>
                <div className="flex items-start">
                  <span className="material-symbols-outlined text-[var(--color-primary)] mr-3 mt-1">
                    settings
                  </span>
                  <div>
                    <h3 className="font-semibold text-[var(--color-textPrimary)] mb-1">
                      Personalized Settings
                    </h3>
                    <p className="text-[var(--color-textSecondary)]">
                      Customize your experience with profile management and theme preferences.
                    </p>
                  </div>
                </div>
              </div>
            </motion.section>

            <motion.section 
              variants={staggerItemVariants}
              className="bg-[var(--color-surface)] rounded-lg p-8 shadow-md border border-[var(--color-border)]"
            >
              <h2 className="text-2xl font-bold text-[var(--color-textPrimary)] mb-4">
                Technology
              </h2>
              <p className="text-[var(--color-textSecondary)] leading-relaxed mb-4">
                Built with modern technologies to ensure a fast, reliable, and secure experience:
              </p>
              <div className="flex flex-wrap gap-3">
                <span className="px-4 py-2 bg-[var(--color-background)] border border-[var(--color-border)] rounded-full text-sm font-medium text-[var(--color-textPrimary)]">
                  React
                </span>
                <span className="px-4 py-2 bg-[var(--color-background)] border border-[var(--color-border)] rounded-full text-sm font-medium text-[var(--color-textPrimary)]">
                  TypeScript
                </span>
                <span className="px-4 py-2 bg-[var(--color-background)] border border-[var(--color-border)] rounded-full text-sm font-medium text-[var(--color-textPrimary)]">
                  AI/ML
                </span>
                <span className="px-4 py-2 bg-[var(--color-background)] border border-[var(--color-border)] rounded-full text-sm font-medium text-[var(--color-textPrimary)]">
                  Spring Boot
                </span>
              </div>
            </motion.section>
          </StaggerContainer>
        </div>
      </main>
    </PageTransition>
  );
};

export default AboutPage;
