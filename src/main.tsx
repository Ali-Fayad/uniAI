import React from 'react'
import ReactDOM from 'react-dom/client'
import AuthForm from './AuthForm'
import './index.css' // If you have one

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <AuthForm />
  </React.StrictMode>,
)
