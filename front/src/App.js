import { AuthProvider } from "./auth/AuthProvider";
import SubOneApp from "./SubOneApp";

export default function App() {
    return (
        <AuthProvider>
            <SubOneApp />
        </AuthProvider>
    );
}
