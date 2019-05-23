import * as React from "react";
import NavigationView from "react-uwp/NavigationView";
import SplitViewCommand from "react-uwp/SplitViewCommand";
import { Theme as UWPThemeProvider, getTheme } from "react-uwp/Theme";



export class App extends React.Component {

    render() {

        return (
        <UWPThemeProvider
            style={{ width: '100%', height: '100%' }}
            theme={getTheme({
            themeName: "dark", // set custom theme
            accent: "#0078D7", // set accent color
            useFluentDesign: true, // sure you want use new fluent design.
            desktopBackgroundImage: require('./res/ad3319b39fdd37b2200f8a58056dd044.jpg') // set global desktop background image
            })}
        >
            <NavigationView
                isControlled={false}
                style={{ height: '100%'}}
                pageTitle="安装探针"
                displayMode="compact"
                autoResize={false}
                defaultExpanded={false}
                navigationTopNodes={[
                    <SplitViewCommand label="账户信息" icon="ContactLegacy" />
                ]}
                navigationBottomNodes={[
                    <SplitViewCommand label="设置" icon={"\uE713"} />
                ]}
                focusNavigationNodeIndex={2}
            >
            </NavigationView>
        </UWPThemeProvider>
        )
    }
}
 
export default App;
