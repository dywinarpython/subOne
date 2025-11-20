import { useAuth } from "./auth/AuthProvider";
import LoadingAnimation from "./components/Loading/LoadingAnimation"
import Header from "./components/Header";
import Footer from "./components/Footer";

export default function SubOneApp() {
    const {loading, user, logout} = useAuth();

    if (loading) {
        return (
            <LoadingAnimation message="Загрузка приложения"/>
        );
    }
    
    return (
      <>
        <Header user = {user} onLogout = {logout}/>
        <Footer/>
      </>
           );
}


