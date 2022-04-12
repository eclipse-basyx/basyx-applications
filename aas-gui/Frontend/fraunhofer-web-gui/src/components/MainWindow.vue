<template>
    <v-container v-if="!isMobile" fluid class="pa-0">
        <div style="display: flex">
            <div class="pa-0 window" :style="{width: aasListWidth}">
                <AASList @minimize="minimize"/>
            </div>
            <div style="position: relative; z-index: 1; height: calc(100vh - 112px);">
                <v-icon style="position: absolute; top: -3px; left: -16px;">mdi-pan-left</v-icon>
                <v-divider vertical style="z-index: 1; height: calc(100vh - 112px); position: absolute;"></v-divider>
                <v-icon style="position: absolute; top: -3px; right: -16px;">mdi-pan-right</v-icon>
            </div>
            <div class="pa-0 window" :style="{'width': '25%' }">
                <SubmodelTree/>
            </div>
            <div style="position: relative; z-index: 1; height: calc(100vh - 112px);">
                <v-icon style="position: absolute; top: -3px; left: -16px;">mdi-pan-left</v-icon>
                <v-divider vertical style="z-index: 1; height: calc(100vh - 112px); position: absolute;"></v-divider>
                <v-icon style="position: absolute; top: -3px; right: -16px;">mdi-pan-right</v-icon>
            </div>
            <div class="pa-0 window" style="width: 18%">
                <PropertyList/>
            </div>
            <div style="position: relative; z-index: 1; height: calc(100vh - 112px);">
                <v-icon style="position: absolute; top: -3px; left: -16px;">mdi-pan-left</v-icon>
                <v-divider vertical style="z-index: 1; height: calc(100vh - 112px); position: absolute;"></v-divider>
                <v-icon style="position: absolute; top: -3px; right: -16px;">mdi-pan-right</v-icon>
            </div>
            <div class="pa-0 window" :style="{width: visuWidth}">
                <ComponentVisu/>
            </div>
        </div>
    </v-container>
    <v-container v-else fluid class="pa-0">
        <v-expansion-panels tile accordion multiple v-model="open">
            <v-expansion-panel>
                <v-expansion-panel-header>Asset Administration Shells</v-expansion-panel-header>
                <v-expansion-panel-content>
                    <AASList @minimize="minimize"/>
                </v-expansion-panel-content>
            </v-expansion-panel>
            <v-expansion-panel>
                <v-expansion-panel-header>Submodels</v-expansion-panel-header>
                <v-expansion-panel-content>
                    <SubmodelTree/>
                </v-expansion-panel-content>
            </v-expansion-panel>
            <v-expansion-panel>
                <v-expansion-panel-header>Properties</v-expansion-panel-header>
                <v-expansion-panel-content>
                    <PropertyList/>
                </v-expansion-panel-content>
            </v-expansion-panel>
            <v-expansion-panel>
                <v-expansion-panel-header>Visualization</v-expansion-panel-header>
                <v-expansion-panel-content>
                    <ComponentVisu/>
                </v-expansion-panel-content>
            </v-expansion-panel>
        </v-expansion-panels>
    </v-container>
</template>

<script>
import AASList          from './AASList.vue';
import SubmodelTree     from './SubmodelTree.vue';
import PropertyList     from './PropertyList.vue';
import ComponentVisu    from './ComponentVisu.vue';
import { isMobile }     from 'mobile-device-detect';
export default {
    name: 'MainWindow',
    components: {
        AASList,
        SubmodelTree,
        PropertyList,
        ComponentVisu,
    },
    data: () => ({
        windows: null,
        open: [0, 1, 2, 3],
        aasListWidth: (100 - ((window.innerWidth - 360) / window.innerWidth) * 100) + '%',
        visuWidth: (100 - (100 - ((window.innerWidth - 360) / window.innerWidth) * 100) - 25 - 18) + '%',
    }),

    mounted() {
        let windows = document.getElementsByClassName('window');
        // console.log(windows)
        for (let i = 0; i < windows.length; i++) {
            if(i != windows.length -1) this.resizableWindow(windows[i]);
        }
    },

    computed: {
        isMobile() {
            return isMobile;
        },
    },

    methods: {
        resizableWindow(window) {
            window.style.position = 'relative';
            // console.log(headerHeight, window);
            let div = this.createDiv(); // create div element for each column
			window.appendChild(div);
            // console.log(window);
            this.setListeners(div); // create event listeners for resize bars
        },
        setListeners(div) {
			let pageX, curCol, nxtCol, curColWidth, nxtColWidth;
			div.addEventListener('mousedown', function(e) {
				curCol = e.target.parentElement; // the current element that will be scaled
				nxtCol = curCol.nextElementSibling; // selects the divider
                if (nxtCol) nxtCol = nxtCol.nextElementSibling; // the next element that will be scaled too
				pageX = e.pageX; // the current x position of the mouse
                // console.log('mouseDown', curCol, nxtCol, pageX);
                curColWidth = parseInt(curCol.offsetWidth);
				if (nxtCol) nxtColWidth = parseInt(nxtCol.offsetWidth);
                // console.log('ColWidth', curColWidth, nxtColWidth);
			});
            // used to highlight the draggable header divider (div)
			div.addEventListener('mouseover', function(e) {
				e.target.style.borderRight = '2px solid #009374';
			});
            // used to remove the highlight of the draggable header divider (div) when the mouse is not over it
			div.addEventListener('mouseout', function(e) {
				e.target.style.borderRight = '';
			});
            // eventlistener to drag the header and scale the columns (windows)
			document.addEventListener('mousemove', function(e) {
                // console.log('mouseMove', curCol);
                let screenWidth = window.innerWidth;
				if (curCol) {
					let diffX = parseInt(e.pageX) - parseInt(pageX); // amount the header was dragged (minus - left, plus - right)
                    // console.log('diffX', screenWidth, curColWidth, diffX, (100 - ((screenWidth - parseInt(curColWidth + diffX)) / screenWidth) * 100) + '%');
					if (nxtCol) nxtCol.style.width = (100 - ((screenWidth - parseInt(nxtColWidth - diffX)) / screenWidth) * 100) + '%'; // scale the next column
					curCol.style.width = (100 - ((screenWidth - parseInt(curColWidth + diffX)) / screenWidth) * 100) + '%'; // scale the current column
				}
			});

			document.addEventListener('mouseup', function() { 
				curCol = undefined;
				nxtCol = undefined;
				pageX = undefined;
				nxtColWidth = undefined;
				curColWidth = undefined
			});
		},
        createDiv(){
			let div = document.createElement('div');
			div.style.top = 0;
			div.style.right = 0;
			div.style.width = '5px';
			div.style.position = 'absolute';
			div.style.cursor = 'col-resize';
			div.style.userSelect = 'none';
			div.style.height = '64px';
            div.style.zIndex = '3';
			return div;
		},
        // minimize functionality for the aas list
        minimize(col) {
            // console.log(col);
            let windows = document.getElementsByClassName('window');
            let screenWidth = window.innerWidth;
            if(col.status && !col.e) {
                let oldWidth = [];
                oldWidth[col.winNr] = parseFloat(windows[col.winNr].style.width);
                oldWidth[col.winNr+1] = parseFloat(windows[col.winNr + 1].style.width);
                // console.log(oldWidth);
                windows[col.winNr].style.width = (100 - ((screenWidth - 60) / screenWidth) * 100) + '%';
                windows[col.winNr+1].style.width = oldWidth[col.winNr+1] + oldWidth[col.winNr] - (100 - ((screenWidth - 60) / screenWidth) * 100) + '%';
            } else if(!col.e) {
                windows[col.winNr].style.width = (100 - ((screenWidth - 360) / screenWidth) * 100) + '%';
                let diffWidth = (100 - ((screenWidth - 360) / screenWidth) * 100) - (100 - ((screenWidth - windows[col.winNr].offsetWidth) / screenWidth) * 100) + '%';
                // console.log('diffWidth: ', diffWidth, windows[col.winNr+1].style.width, windows[col.winNr+2].style.width, windows[col.winNr+3].style.width);
                if(parseFloat(diffWidth) < parseFloat(windows[col.winNr+1].style.width)) {
                    windows[col.winNr+1].style.width = parseFloat(windows[col.winNr+1].style.width) - parseFloat(diffWidth) + '%';
                } else if(parseFloat(diffWidth) < parseFloat(windows[col.winNr+2].style.width)){
                    windows[col.winNr+2].style.width = parseFloat(windows[col.winNr+2].style.width) - parseFloat(diffWidth) + '%';
                } else {
                    windows[col.winNr+3].style.width = parseFloat(windows[col.winNr+3].style.width) - parseFloat(diffWidth) + '%';
                }
            }
        },
    },
}
</script>

<style scoped lang="css">
.v-expansion-panel-content>>> .v-expansion-panel-content__wrap {
  padding: 0 !important;
}
</style>