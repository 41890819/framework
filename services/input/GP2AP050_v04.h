// note! このファイルはライブラリ作成用です。

//GP2AP050.h

#ifndef GP2AP050A_HEADER
#define GP2AP050A_HEADER
// Sensor type
#define SENSOR_NAME_GP2AP050A


#ifdef SENSOR_NAME_GP2AP050A
	#define GESTURE_CODE_NAME_CYGNUS
	#define RGB_CODE_NAME_SYBILLA
#elif defined SENSOR_NAME_GP2AP050S
	#define GESTURE_CODE_NAME_CYGNUS
	#define WITHOUT_ALS
#elif defined SENSOR_NAME_Y2712
	#define GESTURE_CODE_NAME_CYGNUS
	#define ALS_CODE_NAME_SHARAPOVA
#else
	#error That SENSOR_NAME does not exist
#endif

//// Parameters of registers for GESTURE SENSOR
#ifdef GESTURE_CODE_NAME_CYGNUS
	//Register
	#define GS_COMMAND1    		0x00
	#define GS_COMMAND2    		0x01
	#define PS1	    			0x02
	#define PS2	    			0x03
	#define INT_LT_LSB  		0x04
	#define INT_LT_MSB  		0x05
	#define INT_HT_LSB  		0x06
	#define INT_HT_MSB  		0x07
	#define OS_DATA0_LSB   		0x08
	#define OS_DATA0_MSB   		0x09
	#define OS_DATA1_LSB   		0x0A
	#define OS_DATA1_MSB   		0x0B
	#define OS_DATA2_LSB		0x0C
	#define OS_DATA2_MSB		0x0D
	#define OS_DATA3_LSB		0x0E
	#define OS_DATA3_MSB		0x0F
	#define DATA0_LSB			0x10
	#define DATA0_MSB   		0x11
	#define DATA1_LSB   		0x12
	#define DATA1_MSB   		0x13
	#define DATA2_LSB			0x14
	#define DATA2_MSB			0x15
	#define DATA3_LSB			0x16
	#define DATA3_MSB			0x17
	#define DATA4_LSB			0x18
	#define DATA4_MSB			0x19
	#define GS_REV_CODE			0x1A
	#define GS_REG_TEST			0x1B
	
	// COMMAND1
	#define NORMAL_SD			0x80
	#define QUATER_PD_SD		0x90
	#define LED_OFF_SD			0xA0
	#define NORMAL_CON			0xC0
	#define QUATER_PD_CON		0xD0
	#define LED_OFF_CON			0xE0
	#define NO_INT_CLEAR		0x0C
	#define INT_CLEAR			0x00

	
	// COMMAND2
	#ifdef TS1_SAMPLE
		#define GS_INTVAL0		0x00
		#define GS_INTVAL6		0x40
		#define GS_INTVAL25		0x80
		#define GS_INTVAL100	0xC0
	#else
		#define GS_INTVAL0		0x00
		#define GS_INTVAL1		0x40
		#define GS_INTVAL6		0x80
		#define GS_INTVAL25 	0xC0
	#endif
	
	#define INT_SEL_D0			0x00
	#define INT_SEL_D1			0x08
	#define INT_SEL_D2			0x10
	#define INT_SEL_D3			0x18
	#define INT_SEL_D4			0x20
	
	#define INT_FLAG			0x00
	#define INT_PROX			0x04
	
	#define GS_INT_PULSE		0x02
	#define GS_INT_LEVEL		0x00
	
	#define GS_RST				0x01
	#define GS_NO_RST			0x00
	
	// PS1
	#define GS_PRST1			0x00
	#define GS_PRST2			0x20
	#define GS_PRST3			0x40
	#define GS_PRST4			0x60
	#define GS_PRST5			0x80
	#define GS_PRST6			0xA0
	#define GS_PRST7			0xC0
	#define GS_PRST8			0xE0
	
	#define GS_RES14			0x00
	#define GS_RES12			0x08
	#define GS_RES10			0x10
	#define GS_RES8				0x18
	
	#define GS_RANGE1			0x00
	#define GS_RANGE2			0x01
	#define GS_RANGE4			0x02
	#define GS_RANGE8			0x03
	#define GS_RANGE16			0x04
	#define GS_RANGE32			0x05
	#define GS_RANGE64			0x06
	#define GS_RANGE128			0x07
	
	// PS2
	#ifdef TS1_SAMPLE
		#define IS16			0x00
		#define IS32			0x20
		#define IS65			0x40
		#define IS130			0x60
		#define IS180			0xE0
	#else
		#define IS17			0x00
		#define IS35			0x20
		#define IS70			0x40
		#define IS140			0x60
		#define IS190			0xE0
	#endif
	
	#define SUM1				0x00
	#define SUM2				0x04
	#define SUM4				0x08
	#define SUM8				0x0C
	#define SUM16				0x10
	#define SUM32				0x14
	#define SUM64				0x18
	#define SUM128				0x1C
	
	#ifdef TS1_SAMPLE
		#define PULSE_24		0x00
		#define PULSE_8			0x01
		#define PULSE_4			0x02
		#define PULSE_2			0x02
	#else
		#define PULSE_24		0x00
		#define PULSE_16		0x01
		#define PULSE_12		0x02
		#define PULSE_10		0x03
	#endif
#else
	#error The code name does not exist.
#endif

//// Parameters of registers for RGB SENSOR
#ifdef RGB_CODE_NAME_SYBILLA
	//Register
	#define RGB_COMMAND1   	0x00
	#define RGB_COMMAND2   	0x01
	#define RGB1        	0x02
	//#define PS1				0x03
	//#define PS2				0x04
	#define GREEN_LT_LSB	0x05
	#define GREEN_LT_MSB	0x06
	#define GREEN_HT_LSB	0x07
	#define GREEN_HT_MSB	0x08
	//#define PS_LT_LSB		0x09
	//#define PS_LT_MSB		0x0A
	//#define PS_HT_LSB		0x0B
	//#define PS_HT_MSB		0x0C
	//#define CROSSTALK_LSB	0x0D
	//#define CROSSTALK_MSB	0x0E
	//#define DATA_PS_LSB		0x0F
	//#define DATA_PS_MSB		0x10
	#define DATA_G_LSB		0x11
	#define DATA_G_MSB		0x12
	#define DATA_R_LSB		0x13
	#define DATA_R_MSB		0x14
	#define DATA_B_LSB		0x15
	#define DATA_B_MSB		0x16
	#define DATA_B_IR_LSB	0x17
	#define DATA_B_IR_MSB	0x18
	#define RGB_REV			0x19
	#define RGB_TEST		0x1A
	
	//#define PS_RGB_SD		0x80
	#define RGB_SD			0x90
	//#define PS_SD			0xA0
	//#define PS_ALS_CON		0xC0
	#define RGB_CON			0xD0
	//#define PS_CON			0xE0
	
	// COMMAND2
	#define RGB_INTVAL0		0x00
	#define RGB_INTVAL6		0x40
	#define RGB_INTVAL25	0x80
	#define RGB_INTVAL10	0xC0
	
	//#define PIN_INT_AP		0x00
	#define PIN_INT_RGB		0x04
	//#define PIN_INT_PS		0x08
	//#define PIN_PROX		0x0C
	
	#define RGB_INT_PULSE	0x02
	#define RGB_INT_LEVEL	0x00
	
	#define RGB_RST			0x01
	#define RGB_NO_RST		0x00
	
	#define RGB_PRST1		0x00
	#define RGB_PRST2		0x20
	#define RGB_PRST3		0x40
	#define RGB_PRST4		0x60
	#define RGB_PRST5		0x80
	#define RGB_PRST6		0xA0
	#define RGB_PRST7		0xC0
	#define RGB_PRST8		0xE0
	
	#define RGB_RES18		0x00
	#define RGB_RES16		0x08
	#define RGB_RES14		0x10
	#define RGB_RES12		0x18
	
	//#define P_RES14			0x00
	//#define P_RES12			0x08
	//#define P_RES10			0x10
	//#define P_RES8			0x18
	
	#define RGB_RANGE1		0x00
	#define RGB_RANGE2		0x01
	#define RGB_RANGE4		0x02
	#define RGB_RANGE8		0x03
	#define RGB_RANGE16		0x04
	#define RGB_RANGE32		0x05
	#define RGB_RANGE64		0x06
	#define RGB_RANGE128	0x07
	
	//#define IS16			0x00
	//#define IS32			0x10
	//#define IS65			0x20
	//#define IS130			0x30
	//#define IS180			0x70
	
	//#define SUM1			0x00
	//#define SUM2			0x01
	//#define SUM4			0x02
	//#define SUM8			0x03
	//#define SUM16			0x04
	//#define SUM32			0x05
	//#define SUM64			0x06
	//#define SUM128		0x07
#elif defined ALS_CODE_NAME_SHARAPOVA
	#define ALS_COMMAND1	0x00
	#define ALS_COMMAND2	0x01
	#define ALS_COMMAND3	0x02
	#define ALS_COMMAND4	0x03
	#define ALS_LT_LSB		0x04
	#define ALS_LT_MSB		0x05
	#define ALS_HT_LSB		0x06
	#define ALS_HT_MSB		0x07
	//#define PS_LT_LSB		0x08
	//#define PS_LT_MSB		0x09
	//#define PS_HT_LSB		0x0A
	//#define PS_HT_MSB		0x0B
	#define DATA_CLR_LSB	0x0C
	#define DATA_CLR_MSB	0x0D
	#define DATA_IR_LSB		0x0E
	#define DATA_IR_MSB		0x0F
	//#define DATA2_LSB		0x10
	//#define DATA2_MSB		0x11
	//#define DATA3_LSB		0x12
	//#define DATA3_MSB		0x13
	//#define DATA4_LSB		0x14
	//#define DATA4_MSB		0x15
	//#define REG_SUM		0x16
	#define REG_REV			0x17
	#define REG_TEST		0x18

	//#define PS_ALS_SD		0x80
	#define ALS_SD			0x90
	//#define PS_SD			0xA0
	//#define RESERVED_SD	0xB0
	//#define PS_ALS_CON	0xC0
	#define ALS_CON			0xD0
	//#define PS_CON		0xE0
	//#define RESERVED_CON	0xF0

	//#define PRST1			0x00
	//#define PRST4			0x40
	//#define PRST8			0x80
	//#define PRST16		0xC0

	#define ALS_RES19		0x00
	#define ALS_RES18		0x08
	#define ALS_RES17		0x10
	#define ALS_RES16		0x18
	#define ALS_RES14		0x20
	#define ALS_RES12		0x28
	#define ALS_RES10		0x30
	#define ALS_RES8		0x38

	//#define P_RES16		0x00
	//#define P_RES14		0x08
	//#define P_RES12		0x10
	//#define P_RES10		0x18
	//#define P_RES8		0x20
	//#define P_RES6		0x28
	//#define P_RES4		0x30
	//#define P_RES2		0x38

	#define ALS_RANGE1		0x00
	#define ALS_RANGE2		0x01
	#define ALS_RANGE4		0x02
	#define ALS_RANGE8		0x03
	#define ALS_RANGE16		0x04
	#define ALS_RANGE32		0x05
	#define ALS_RANGE64		0x06
	#define ALS_RANGE128	0x07

	//#define ALC			0x80
	//#define ALC_OFF		0x00
	#define ALS_INT_PULSE	0x40
	#define ALS_INT_LEVEL	0x00

	#define ALS_INTVAL0		0x00
	#define ALS_INTVAL4		0x40
	#define ALS_INTVAL8		0x80
	#define ALS_INTVAL16	0xC0

	//#define IS138			0x00
	//#define IS275			0x10
	//#define IS55			0x20
	//#define IS110			0x30

	//#define PIN_INT_AP	0x00
	#define PIN_INT_ALS		0x04
	//#define PIN_INT_PS	0x08
	//#define PIN_PROX		0x0C

	//#define FREQ0			0x00
	//#define FREQ1			0x02
	#define ALS_RST			0x01
	#define ALS_NO_RST		0x00

#elif defined WITHOUT_ALS
	//No needed parameters for ALS
#else
	#error The code name does not exist.
#endif

#endif//GP2AP050A_HEADER